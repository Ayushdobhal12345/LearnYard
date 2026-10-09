import { useState, useEffect } from 'react';
import StatusMessage from '../components/StatusMessage';
import PageHeader from '../components/PageHeader';
import { api, getCurrentStudentId, getCurrentSubjectId } from '../api/client';

function PlanItemRow({ item, rank, last }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 18, padding: '18px 0', borderBottom: last ? 'none' : '1px solid var(--border)' }}>
      <span className="tabular" style={{ fontSize: 12, fontWeight: 700, color: 'var(--gray)', width: 20 }}>{rank}</span>
      <span style={{ flex: 1, fontSize: 17, fontWeight: 700 }}>{item.topicName}</span>
      <span className="tabular" style={{ fontSize: 12, fontWeight: 700, color: 'var(--gray)' }}>
        priority {Math.round(item.priorityScoreSnapshot)}
      </span>
      <span className="tabular" style={{ fontSize: 20, fontWeight: 800, color: 'var(--blue-deep)', width: 60, textAlign: 'right' }}>
        {item.allocatedHours}h
      </span>
    </div>
  );
}

function PlanCard({ title, plan, dark }) {
  return (
    <div
      style={{
        flex: 1,
        background: dark ? 'var(--dark)' : 'var(--surface)',
        border: dark ? 'none' : '1px solid var(--border)',
        borderRadius: 'var(--radius-lg)',
        padding: '30px 32px',
        color: dark ? '#fff' : 'var(--dark)',
      }}
    >
      <div style={{ fontSize: 11.5, fontWeight: 700, letterSpacing: '0.06em', color: dark ? 'var(--lime)' : 'var(--gray)', marginBottom: 14 }}>
        {title}
      </div>
      {!plan ? (
        <p style={{ fontSize: 14, color: dark ? 'rgba(255,255,255,0.6)' : 'var(--gray)' }}>No plan yet.</p>
      ) : (
        <div>
          <div style={{ fontSize: 13, color: dark ? 'rgba(255,255,255,0.6)' : 'var(--gray)', marginBottom: 6 }}>
            {plan.availableHours}h available{plan.hoursUntilExam ? ` \u00b7 exam in ${plan.hoursUntilExam}h` : ''}
          </div>
          {plan.items.map((item, i) => (
            <PlanItemRow key={item.topicId} item={item} rank={i + 1} last={i === plan.items.length - 1} />
          ))}
        </div>
      )}
    </div>
  );
}

function StudyPlanPage() {
  const [activePlan, setActivePlan] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [generating, setGenerating] = useState(false);
  const [hours, setHours] = useState(5);
  const [examHours, setExamHours] = useState(24);

const loadAll = () => {
    setLoading(true);
    Promise.all([
      api.getActivePlan(getCurrentStudentId(), getCurrentSubjectId()).catch(() => null),
      api.getPlanHistory(getCurrentStudentId(), getCurrentSubjectId()).catch(() => []),
    ]).then(([active, hist]) => { setActivePlan(active); setHistory(hist); })
      .catch((err) => setError(err.message)).finally(() => setLoading(false));
};

  useEffect(() => { loadAll(); }, []);

  const handleGenerate = (e) => {
    e.preventDefault();
    setGenerating(true);
    setError('');
    api.generatePlan({ studentId: getCurrentStudentId(), subjectId: getCurrentSubjectId(), availableHours: Number(hours), hoursUntilExam: Number(examHours) })
      .then(() => loadAll()).catch((err) => setError(err.message)).finally(() => setGenerating(false));
  };

  if (loading) return <StatusMessage loading={loading} />;

  const previousPlan = history.find((p) => !p.isActive);

  return (
    <div>
      <PageHeader eyebrow="Planning" title="Time, allocated intelligently." subtitle="Hours distributed across topics based on live priority." />

      <form
        onSubmit={handleGenerate}
        style={{ display: 'flex', alignItems: 'flex-end', gap: 20, padding: '26px 30px', background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', marginBottom: 28 }}
      >
        <div>
          <label style={{ display: 'block', fontSize: 12, color: 'var(--gray)', marginBottom: 6, fontWeight: 600 }}>Hours available</label>
          <input type="number" min="0.5" step="0.5" value={hours} onChange={(e) => setHours(e.target.value)}
            style={{ width: 90, padding: '10px 12px', border: '1px solid var(--border-strong)', borderRadius: 10, fontFamily: 'var(--font)', fontSize: 15, fontWeight: 600 }} />
        </div>
        <div>
          <label style={{ display: 'block', fontSize: 12, color: 'var(--gray)', marginBottom: 6, fontWeight: 600 }}>Hours until exam</label>
          <input type="number" min="1" value={examHours} onChange={(e) => setExamHours(e.target.value)}
            style={{ width: 90, padding: '10px 12px', border: '1px solid var(--border-strong)', borderRadius: 10, fontFamily: 'var(--font)', fontSize: 15, fontWeight: 600 }} />
        </div>
        <button type="submit" disabled={generating}
          style={{ padding: '12px 26px', fontSize: 14, fontWeight: 700, color: 'var(--dark)', background: 'var(--lime)', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
          {generating ? 'Generating…' : 'Generate plan'}
        </button>
      </form>

      {error && <StatusMessage error={error} />}

      {previousPlan ? (
        <div style={{ display: 'flex', gap: 20 }}>
          <PlanCard title="PREVIOUS" plan={previousPlan} />
          <PlanCard title="CURRENT" plan={activePlan} dark />
        </div>
      ) : (
        <PlanCard title="CURRENT" plan={activePlan} dark />
      )}
    </div>
  );
}

export default StudyPlanPage;