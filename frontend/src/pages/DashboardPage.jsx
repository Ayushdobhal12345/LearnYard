import { useState, useEffect } from 'react';
import { api, getCurrentStudentId } from '../api/client';
import StatusMessage from '../components/StatusMessage';
import PageHeader from '../components/PageHeader';
import Markdown from '../components/Markdown';

function MasteryDial({ percentage }) {
  const radius = 34;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (percentage / 100) * circumference;
  return (
    <div style={{ position: 'relative', width: 80, height: 80 }}>
      <svg width="80" height="80" viewBox="0 0 80 80" style={{ transform: 'rotate(-90deg)' }}>
        <circle cx="40" cy="40" r={radius} fill="none" stroke="rgba(0,0,0,0.08)" strokeWidth="7" />
        <circle cx="40" cy="40" r={radius} fill="none" stroke="var(--lime)" strokeWidth="7"
          strokeDasharray={circumference} strokeDashoffset={offset} strokeLinecap="round" />
      </svg>
      <div className="tabular" style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 17, fontWeight: 800 }}>
        {Math.round(percentage)}%
      </div>
    </div>
  );
}

function Metric({ label, value, tone }) {
  const color = tone === 'danger' ? '#c0392b' : tone === 'positive' ? 'var(--lime-deep)' : 'var(--dark)';
  return (
    <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '20px 22px' }}>
      <div style={{ fontSize: 12.5, color: 'var(--gray)', marginBottom: 12 }}>{label}</div>
      <div className="tabular" style={{ fontSize: 30, fontWeight: 800, color }}>{value}</div>
    </div>
  );
}

function DashboardPage() {
  const [data, setData] = useState(null);
  const [insight, setInsight] = useState('');
  const [insightLoading, setInsightLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const studentId = getCurrentStudentId();
    api.getDashboard(studentId)
      .then((res) => setData(res))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleGenerateInsight = () => {
    setInsightLoading(true);
    setInsight('');
    api.getWeeklyInsight(getCurrentStudentId())
      .then((r) => setInsight(r.insight))
      .catch((err) => setInsight(err.message || "Couldn't load your summary right now."))
      .finally(() => setInsightLoading(false));
  };

  if (loading || error) return <StatusMessage loading={loading} error={error} />;
  if (!data) return null;

  return (
    <div>
      <PageHeader eyebrow="Overview" title="Your learning, quantified." subtitle="Updated automatically after every quiz." />

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 18, marginBottom: 20 }}>
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '18px 20px', display: 'flex', alignItems: 'center', gap: 16 }}>
          <MasteryDial percentage={data.overallMasteryPercentage ?? 0} />
          <div>
            <div style={{ fontSize: 12.5, color: 'var(--gray)' }}>Overall mastery</div>
            <div style={{ fontSize: 12.5, color: 'var(--dark)', marginTop: 2 }}>across tracked topics</div>
          </div>
        </div>
        <Metric label="Weak topics" value={data.weakTopicsCount ?? 0} tone={data.weakTopicsCount > 0 ? 'danger' : 'positive'} />
        <Metric label="Due for revision" value={data.topicsDueForRevisionCount ?? 0} />
        <Metric label="Study streak" value={`${data.streakDays ?? 0}d`} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 18 }}>
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '22px 24px' }}>
          <div style={{ fontSize: 15, fontWeight: 800, marginBottom: 16 }}>Weak topics</div>
          {(!data.weakTopicNames || data.weakTopicNames.length === 0) ? (
            <p style={{ fontSize: 14, color: 'var(--gray)', margin: 0 }}>None right now — nice work.</p>
          ) : (
            data.weakTopicNames.map((name, i) => (
              <div key={name} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '13px 0', borderBottom: i < data.weakTopicNames.length - 1 ? '1px solid var(--border)' : 'none' }}>
                <span style={{ fontSize: 14, fontWeight: 600 }}>{name}</span>
                <span style={{ fontSize: 11, fontWeight: 700, color: '#c0392b', background: '#fce8e6', padding: '4px 11px', borderRadius: 6 }}>
                  NEEDS FOCUS
                </span>
              </div>
            ))
          )}
        </div>

        <div style={{ background: 'var(--dark)', borderRadius: 'var(--radius-lg)', padding: '22px 24px', color: '#fff' }}>
          <div style={{ fontSize: 12.5, opacity: 0.6, marginBottom: 8 }}>AI weekly check-in</div>
          {insight && <p style={{ fontSize: 14, lineHeight: 1.6, margin: '0 0 14px' }}><Markdown text={insight} inline /></p>}
          <button
            type="button"
            onClick={handleGenerateInsight}
            disabled={insightLoading}
            style={{
              padding: '8px 16px', fontSize: 13, fontWeight: 700, background: 'var(--lime)',
              color: 'var(--dark)', border: 'none', borderRadius: 10,
              cursor: insightLoading ? 'wait' : 'pointer', opacity: insightLoading ? 0.6 : 1
            }}
          >
            {insightLoading ? 'Thinking…' : insight ? 'Refresh check-in' : 'Generate my check-in'}
          </button>
        </div>
      </div>

      <p style={{ marginTop: 18, fontSize: 12.5, color: 'var(--gray)' }}>
        Active study plan: {data.hasActiveStudyPlan ? 'yes' : 'none yet'} · Quiz attempts: {data.totalQuizAttempts ?? 0}
      </p>
    </div>
  );
}

export default DashboardPage;