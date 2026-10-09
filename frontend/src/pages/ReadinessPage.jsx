import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId, getCurrentStudentId } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusMessage from '../components/StatusMessage';

const card = {
  background: 'var(--surface)',
  border: '1px solid var(--border)',
  borderRadius: 'var(--radius-lg)',
  padding: '26px 28px',
};

const kicker = {
  fontSize: 12.5,
  fontWeight: 700,
  color: 'var(--gray)',
  letterSpacing: '0.08em',
  textTransform: 'uppercase',
  marginBottom: 14,
};

function storageKey(subjectId) {
  return `ila_exam_date_${subjectId}`;
}

function loadSavedDate(subjectId) {
  try {
    return window.localStorage.getItem(storageKey(subjectId)) || '';
  } catch {
    return '';
  }
}

function daysUntil(dateText) {
  if (!dateText) return null;
  const target = new Date(`${dateText}T00:00:00`);
  if (Number.isNaN(target.getTime())) return null;
  return Math.ceil((target.getTime() - Date.now()) / (24 * 60 * 60 * 1000));
}

function sign(value) {
  return value > 0 ? `+${value}` : `${value}`;
}

function masteryText(item) {
  return item.practiced ? `${item.mastery}% mastery` : 'not practised yet';
}

function ReadinessPage() {
  const studentId = getCurrentStudentId();
  const subjectId = getCurrentSubjectId();

  const [examDate, setExamDate] = useState(() => loadSavedDate(subjectId));
  const [readiness, setReadiness] = useState(null);
  const [causes, setCauses] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const daysLeft = daysUntil(examDate);
    api.getReadiness(studentId, subjectId, daysLeft && daysLeft > 0 ? daysLeft : null)
      .then(setReadiness)
      .catch((err) => setError(err.message));
  }, [studentId, subjectId, examDate]);

  useEffect(() => {
    api.getRootCauses(studentId, subjectId)
      .then(setCauses)
      .catch((err) => setError(err.message));
  }, [studentId, subjectId]);

  const handleDateChange = (value) => {
    setExamDate(value);
    try {
      window.localStorage.setItem(storageKey(subjectId), value);
    } catch {
      // storage unavailable: the date just won't be remembered
    }
  };

  if (error) return <StatusMessage loading={false} error={error} />;
  if (!readiness || !causes) return <StatusMessage loading error="" />;

  const score = Math.round(readiness.readiness);
  const days = daysUntil(examDate);
  const hasTopics = readiness.totalTopics > 0;
  const barColor = score >= 80 ? '#7bb83a' : score >= 60 ? 'var(--lime)' : score >= 35 ? '#e0b34a' : '#e07a5f';

  return (
    <div>
      <PageHeader
        eyebrow="Readiness"
        title="Exam readiness."
        subtitle="How prepared you are right now, what is holding you back, and where to start."
      />

      {!hasTopics ? (
        <div style={card}>
          <div style={{ fontSize: 15, color: 'var(--gray)' }}>
            This subject has no topics yet. Add your units and topics under Curriculum, then come back.
          </div>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 22 }}>
          {/* ---------- Score ---------- */}
          <div style={{ background: 'var(--dark)', color: '#fff', borderRadius: 'var(--radius-lg)', padding: '30px 32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: 24, flexWrap: 'wrap', alignItems: 'flex-end' }}>
              <div>
                <div style={{ fontSize: 12.5, opacity: 0.6, marginBottom: 6, letterSpacing: '0.08em', textTransform: 'uppercase', fontWeight: 700 }}>
                  Readiness score
                </div>
                <div className="tabular" style={{ fontSize: 76, fontWeight: 800, letterSpacing: '-0.03em', lineHeight: 1 }}>
                  {score}%
                </div>
                <div style={{ fontSize: 15, fontWeight: 700, color: 'var(--lime)', marginTop: 8 }}>{readiness.label}</div>
              </div>

              <div style={{ display: 'flex', gap: 28, flexWrap: 'wrap' }}>
                <div>
                  <div className="tabular" style={{ fontSize: 24, fontWeight: 800 }}>{sign(readiness.weeklyChange)}</div>
                  <div style={{ fontSize: 12, opacity: 0.6 }}>points in 7 days</div>
                </div>
                <div>
                  <div className="tabular" style={{ fontSize: 24, fontWeight: 800 }}>
                    {readiness.practicedTopics}/{readiness.totalTopics}
                  </div>
                  <div style={{ fontSize: 12, opacity: 0.6 }}>topics practised</div>
                </div>
                {readiness.projected !== undefined && (
                  <div>
                    <div className="tabular" style={{ fontSize: 24, fontWeight: 800, color: 'var(--lime)' }}>
                      {Math.round(readiness.projected)}%
                    </div>
                    <div style={{ fontSize: 12, opacity: 0.6 }}>projected in {readiness.daysLeft} days</div>
                  </div>
                )}
              </div>
            </div>

            <div style={{ height: 10, borderRadius: 99, background: 'rgba(255,255,255,0.12)', marginTop: 26, overflow: 'hidden' }}>
              <div style={{ width: `${Math.min(100, score)}%`, height: '100%', background: barColor, transition: 'width 0.5s ease' }} />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap', marginTop: 22 }}>
              <label htmlFor="exam-date" style={{ fontSize: 13, opacity: 0.75 }}>Exam date</label>
              <input
                id="exam-date"
                type="date"
                value={examDate}
                onChange={(e) => handleDateChange(e.target.value)}
                style={{ padding: '8px 12px', borderRadius: 9, border: '1px solid rgba(255,255,255,0.2)', background: 'rgba(255,255,255,0.08)', color: '#fff', fontSize: 13.5, fontFamily: 'var(--font)', colorScheme: 'dark' }}
              />
              <span style={{ fontSize: 12.5, opacity: 0.6 }}>
                {days === null && 'Add your exam date to see where you will be on the day.'}
                {days !== null && days > 0 && `${days} day${days === 1 ? '' : 's'} to go. The projection assumes you keep your last 7 days' pace.`}
                {days !== null && days <= 0 && 'That date has passed. Pick your next exam date.'}
              </span>
            </div>
          </div>

          {/* ---------- Start here ---------- */}
          <div style={card}>
            <div style={kicker}>Start here</div>
            {causes.startHere.length === 0 ? (
              <div style={{ fontSize: 14.5, color: 'var(--gray)', lineHeight: 1.6 }}>
                {causes.findings.length === 0
                  ? 'No weak topics right now. Keep practising to hold your score.'
                  : 'Your prerequisites look fine. The topics below need direct practice.'}
              </div>
            ) : (
              <div style={{ display: 'grid', gap: 14 }}>
                {causes.startHere.map((item) => (
                  <div key={item.topicId} style={{ display: 'flex', gap: 16, alignItems: 'flex-start', paddingBottom: 14, borderBottom: '1px solid var(--border)' }}>
                    <div style={{ width: 34, height: 34, borderRadius: 10, background: 'var(--lime)', color: 'var(--dark)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, flexShrink: 0 }}>
                      {item.blocks.length}
                    </div>
                    <div>
                      <div style={{ fontSize: 16, fontWeight: 700 }}>{item.topicName}</div>
                      <div style={{ fontSize: 13, color: 'var(--gray)', margin: '2px 0 6px' }}>{masteryText(item)}</div>
                      <div style={{ fontSize: 14, lineHeight: 1.55 }}>
                        This is the earliest gap behind {item.blocks.length === 1 ? 'a weak topic' : `${item.blocks.length} weak topics`}:{' '}
                        {item.blocks.join(', ')}. Fixing it first should make those easier.
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* ---------- Why topics are weak ---------- */}
          {causes.findings.length > 0 && (
            <div style={card}>
              <div style={kicker}>Why these topics are weak</div>
              <div style={{ display: 'grid', gap: 12 }}>
                {causes.findings.slice(0, 8).map((f) => (
                  <div key={f.topicId} style={{ fontSize: 14.5, lineHeight: 1.55 }}>
                    <b>{f.topicName}</b> <span style={{ color: 'var(--gray)' }}>({f.mastery}%)</span>
                    {' · '}
                    {f.verdict === 'prerequisite_gap'
                      ? `held back by ${f.rootCauses.map((r) => r.topicName).join(', ')}`
                      : 'your prerequisites are fine, so this topic needs direct practice'}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ---------- Biggest wins ---------- */}
          {readiness.biggestWins.length > 0 && (
            <div style={card}>
              <div style={kicker}>Biggest wins</div>
              <div style={{ fontSize: 13.5, color: 'var(--gray)', marginBottom: 14 }}>
                Points your readiness would gain if you mastered each topic.
              </div>
              <div style={{ display: 'grid', gap: 10 }}>
                {readiness.biggestWins.map((w) => (
                  <div key={w.topicId} style={{ display: 'flex', justifyContent: 'space-between', gap: 16, fontSize: 14.5 }}>
                    <div>
                      <b>{w.topicName}</b> <span style={{ color: 'var(--gray)' }}>· {masteryText(w)}</span>
                    </div>
                    <div className="tabular" style={{ fontWeight: 800, color: '#3f7d1f', whiteSpace: 'nowrap' }}>+{w.gain}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <p style={{ fontSize: 12.5, color: 'var(--gray)', lineHeight: 1.6, margin: 0 }}>
            How it is calculated: each topic counts by its mastery, lowered up to 30% if you have not
            revised it for a while, and topics that appear often in past papers count up to twice as much.
            Topics you have not practised yet count as zero.
          </p>
        </div>
      )}
    </div>
  );
}

export default ReadinessPage;
