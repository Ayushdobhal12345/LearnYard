import { useState, useEffect, useRef, useCallback } from 'react';
import { api, getCurrentSubjectId, getCurrentStudentId } from '../api/client';
import PageHeader from '../components/PageHeader';
import { useToast } from '../context/ToastContext';

const PRESETS = [15, 25, 45, 60];

function formatClock(totalSeconds) {
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function FocusPage() {
  const showToast = useToast();
  const studentId = getCurrentStudentId();

  const [topics, setTopics] = useState([]);
  const [topicId, setTopicId] = useState('');
  const [minutes, setMinutes] = useState(25);
  const [secondsLeft, setSecondsLeft] = useState(25 * 60);
  const [running, setRunning] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [sessions, setSessions] = useState([]);
  const [stats, setStats] = useState({ todayMinutes: 0, weekMinutes: 0, total: 0 });

  const endAtRef = useRef(null);

  const studiedSeconds = minutes * 60 - secondsLeft;
  const idle = !running && studiedSeconds === 0;

  const loadSessions = useCallback(() => {
    if (!studentId) return;
    api.getStudySessions(studentId)
      .then((list) => {
        const now = new Date();
        const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
        const weekAgo = startOfToday - 6 * 24 * 60 * 60 * 1000;
        let today = 0;
        let week = 0;
        list.forEach((s) => {
          const t = new Date(s.sessionDate).getTime();
          if (t >= startOfToday) today += s.durationMinutes;
          if (t >= weekAgo) week += s.durationMinutes;
        });
        setSessions(list);
        setStats({ todayMinutes: today, weekMinutes: week, total: list.length });
      })
      .catch(() => {});
  }, [studentId]);

  useEffect(() => {
    api.getTopicsForSubject(getCurrentSubjectId())
      .then((t) => {
        setTopics(t);
        if (t.length > 0) setTopicId(String(t[0].id));
      })
      .catch((err) => setError(err.message));
    loadSessions();
  }, [loadSessions]);

  const resetTimer = useCallback((mins) => {
    setRunning(false);
    endAtRef.current = null;
    setSecondsLeft(mins * 60);
  }, []);

  const logSession = useCallback((seconds) => {
    const minutesToLog = Math.max(1, Math.round(seconds / 60));
    setSaving(true);
    setError('');
    api.logStudySession(studentId, Number(topicId), minutesToLog)
      .then(() => {
        showToast(`Session logged: ${minutesToLog} min`);
        loadSessions();
      })
      .catch((err) => setError(err.message))
      .finally(() => setSaving(false));
  }, [studentId, topicId, showToast, loadSessions]);

  // Countdown. The end time lives in a ref so pausing/resuming never drifts.
  useEffect(() => {
    if (!running) return undefined;
    const id = setInterval(() => {
      const remaining = Math.max(0, Math.round((endAtRef.current - Date.now()) / 1000));
      setSecondsLeft(remaining);
      if (remaining === 0) {
        clearInterval(id);
        setRunning(false);
        logSession(minutes * 60);
        setSecondsLeft(minutes * 60);
      }
    }, 500);
    return () => clearInterval(id);
  }, [running, minutes, logSession]);

  const chooseMinutes = (m) => {
    if (!idle) return;
    const clean = Math.min(180, Math.max(1, Number(m) || 1));
    setMinutes(clean);
    setSecondsLeft(clean * 60);
  };

  const handleStart = () => {
    if (!topicId) {
      setError('Pick a topic first. Add topics under Curriculum if the list is empty.');
      return;
    }
    setError('');
    endAtRef.current = Date.now() + secondsLeft * 1000;
    setRunning(true);
  };

  const handlePause = () => setRunning(false);

  const handleFinishEarly = () => {
    if (studiedSeconds < 60) {
      showToast('Study for at least a minute before logging.', 'error');
      return;
    }
    const seconds = studiedSeconds;
    resetTimer(minutes);
    logSession(seconds);
  };

  const progress = minutes * 60 === 0 ? 0 : Math.min(100, (studiedSeconds / (minutes * 60)) * 100);
  const selectStyle = { padding: '10px 14px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14, fontFamily: 'var(--font)', background: '#fff' };
  const btn = { padding: '12px 24px', fontSize: 14, fontWeight: 700, borderRadius: 11, border: 'none', cursor: 'pointer' };

  return (
    <div>
      <PageHeader
        eyebrow="Focus"
        title="Focus timer."
        subtitle="Study in focused blocks. Every logged session counts toward your streak."
      />

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.4fr) minmax(0, 1fr)', gap: 24, alignItems: 'start' }}>
        {/* Timer card */}
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: 32 }}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 24 }}>
            <select
              value={topicId}
              onChange={(e) => setTopicId(e.target.value)}
              disabled={!idle}
              style={{ ...selectStyle, minWidth: 220 }}
            >
              {topics.length === 0 && <option value="">No topics yet</option>}
              {topics.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {PRESETS.map((m) => (
                <button
                  key={m}
                  onClick={() => chooseMinutes(m)}
                  disabled={!idle}
                  style={{
                    padding: '9px 16px', borderRadius: 10, fontSize: 13.5, fontWeight: 700,
                    border: '1px solid var(--border-strong)', cursor: idle ? 'pointer' : 'not-allowed',
                    background: minutes === m ? 'var(--dark)' : '#fff',
                    color: minutes === m ? 'var(--lime)' : 'var(--dark)',
                    opacity: idle ? 1 : 0.6,
                  }}
                >
                  {m} min
                </button>
              ))}
              <input
                type="number" min={1} max={180} value={minutes}
                onChange={(e) => chooseMinutes(e.target.value)}
                disabled={!idle}
                aria-label="Custom minutes"
                style={{ ...selectStyle, width: 84 }}
              />
            </div>
          </div>

          <div style={{ textAlign: 'center', padding: '28px 0 22px' }}>
            <div className="tabular" style={{ fontSize: 88, fontWeight: 800, letterSpacing: '-0.03em', lineHeight: 1 }}>
              {formatClock(secondsLeft)}
            </div>
            <div style={{ height: 8, background: 'var(--border)', borderRadius: 99, overflow: 'hidden', maxWidth: 420, margin: '26px auto 0' }}>
              <div style={{ width: `${progress}%`, height: '100%', background: 'var(--lime-deep)', transition: 'width 0.4s ease' }} />
            </div>
          </div>

          <div style={{ display: 'flex', gap: 10, justifyContent: 'center', flexWrap: 'wrap' }}>
            {!running ? (
              <button onClick={handleStart} disabled={saving} style={{ ...btn, background: 'var(--lime)', color: 'var(--dark)', opacity: saving ? 0.6 : 1 }}>
                {idle ? 'Start focus session' : 'Resume'}
              </button>
            ) : (
              <button onClick={handlePause} style={{ ...btn, background: 'var(--dark)', color: '#fff' }}>Pause</button>
            )}
            <button
              onClick={handleFinishEarly}
              disabled={idle || saving}
              style={{ ...btn, background: '#fff', color: 'var(--dark)', border: '1px solid var(--border-strong)', opacity: idle || saving ? 0.5 : 1 }}
            >
              Finish and log
            </button>
            <button
              onClick={() => resetTimer(minutes)}
              disabled={idle}
              style={{ ...btn, background: 'transparent', color: 'var(--gray)', opacity: idle ? 0.5 : 1 }}
            >
              Reset
            </button>
          </div>

          {error && <p style={{ fontSize: 13, color: '#c0392b', textAlign: 'center', marginTop: 18, marginBottom: 0 }}>{error}</p>}
        </div>

        {/* Stats + history */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12 }}>
            {[
              { label: 'Today', value: `${stats.todayMinutes}m` },
              { label: 'Last 7 days', value: `${stats.weekMinutes}m` },
              { label: 'Sessions', value: stats.total },
            ].map((s) => (
              <div key={s.label} style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '16px 14px' }}>
                <div className="tabular" style={{ fontSize: 24, fontWeight: 800 }}>{s.value}</div>
                <div style={{ fontSize: 12, color: 'var(--gray)', marginTop: 2 }}>{s.label}</div>
              </div>
            ))}
          </div>

          <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '22px 24px' }}>
            <div style={{ fontSize: 12.5, fontWeight: 700, color: 'var(--gray)', letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 14 }}>
              Recent sessions
            </div>
            {sessions.length === 0 ? (
              <p style={{ fontSize: 14, color: 'var(--gray)', margin: 0 }}>No sessions yet. Start your first one.</p>
            ) : (
              sessions.slice(0, 6).map((s) => (
                <div key={s.id} style={{ display: 'flex', justifyContent: 'space-between', gap: 12, padding: '10px 0', borderTop: '1px solid var(--border)', fontSize: 14 }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{s.topicName}</div>
                    <div style={{ fontSize: 12, color: 'var(--gray)' }}>{new Date(s.sessionDate).toLocaleString()}</div>
                  </div>
                  <div className="tabular" style={{ fontWeight: 700, whiteSpace: 'nowrap' }}>{s.durationMinutes} min</div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default FocusPage;
