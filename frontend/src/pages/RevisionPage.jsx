import { useState, useEffect } from 'react';
import { api, getCurrentStudentId, getCurrentSubjectId } from '../api/client';
import StatusMessage from '../components/StatusMessage';
import PageHeader from '../components/PageHeader';

function RevisionPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [markingId, setMarkingId] = useState(null);

  const load = () => {
    setLoading(true);
    api.getDueForRevision(getCurrentStudentId(), getCurrentSubjectId()).then(setItems).catch((err) => setError(err.message)).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleMarkRevised = (topicId) => {
    setMarkingId(topicId);
    api.markRevised(getCurrentStudentId(), topicId).then(() => load()).catch((err) => setError(err.message)).finally(() => setMarkingId(null));
  };

  if (loading || error) return <StatusMessage loading={loading} error={error} />;

  return (
    <div>
      <PageHeader eyebrow="Retention" title="Before you forget." subtitle="Topics at risk, based on time since last practiced." />

      {items.length === 0 ? (
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '48px', textAlign: 'center' }}>
          <p style={{ fontSize: 16, color: 'var(--gray)', margin: 0 }}>Nothing due for revision right now.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {items.map((item) => (
            <div
              key={item.topicId}
              style={{
                display: 'flex', alignItems: 'center', gap: 24, padding: '26px 30px',
                background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)',
              }}
            >
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 20, fontWeight: 700, marginBottom: 6 }}>{item.topicName}</div>
                <div style={{ fontSize: 13.5, color: 'var(--gray)' }}>
                  mastery <span className="tabular" style={{ fontWeight: 700, color: 'var(--dark)' }}>{Math.round(item.masteryScore)}%</span>
                </div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div className="tabular" style={{ fontSize: 26, fontWeight: 800, color: 'var(--blue-deep)' }}>{Math.round(item.forgettingRisk)}</div>
                <div style={{ fontSize: 10.5, color: 'var(--gray)', letterSpacing: '0.05em', fontWeight: 700 }}>RISK SCORE</div>
              </div>
              <button
                onClick={() => handleMarkRevised(item.topicId)}
                disabled={markingId === item.topicId}
                style={{ padding: '11px 22px', fontSize: 13.5, fontWeight: 700, color: 'var(--dark)', background: 'var(--lime)', border: 'none', borderRadius: 10, cursor: 'pointer' }}
              >
                {markingId === item.topicId ? 'Marking…' : 'Mark revised'}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default RevisionPage;