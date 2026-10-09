import { useState, useEffect } from 'react';
import { api, getCurrentStudentId, getCurrentSubjectId } from '../api/client';
import StatusMessage from '../components/StatusMessage';
import PageHeader from '../components/PageHeader';

function BreakdownRow({ label, value }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', fontSize: 13.5 }}>
      <span style={{ color: 'var(--gray)' }}>{label}</span>
      <span className="tabular" style={{ fontWeight: 700 }}>{Math.round(value)}</span>
    </div>
  );
}

function PriorityPage() {
  const [ranking, setRanking] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedTopicId, setExpandedTopicId] = useState(null);

  useEffect(() => {
api.getPriorityRanking(getCurrentStudentId(), getCurrentSubjectId()).then(setRanking).catch((err) => setError(err.message)).finally(() => setLoading(false));
  }, []);

  if (loading || error) return <StatusMessage loading={loading} error={error} />;

  return (
    <div>
      <PageHeader
        eyebrow="Priority engine"
        title="Ranked by what matters most."
        subtitle="Weakness, exam relevance, prerequisites, and forgetting risk, combined into one score."
      />

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        {ranking.map((item, i) => {
          const isExpanded = expandedTopicId === item.topicId;
          const isTop = i === 0;
          return (
            <div
              key={item.topicId}
              style={{
                gridColumn: isTop ? '1 / -1' : 'auto',
                background: isTop ? 'var(--dark)' : 'var(--surface)',
                border: isTop ? 'none' : '1px solid var(--border)',
                borderRadius: 'var(--radius-lg)',
                padding: '28px 30px',
                cursor: 'pointer',
              }}
              onClick={() => setExpandedTopicId(isExpanded ? null : item.topicId)}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: isExpanded ? 20 : 0 }}>
                <div>
                  {isTop && (
                    <div style={{ fontSize: 11.5, fontWeight: 700, color: 'var(--lime)', letterSpacing: '0.06em', marginBottom: 8 }}>
                      TOP PRIORITY
                    </div>
                  )}
                  <div style={{ fontSize: isTop ? 32 : 21, fontWeight: 800, color: isTop ? '#fff' : 'var(--dark)', letterSpacing: '-0.01em' }}>
                    {item.topicName}
                  </div>
                </div>
                <div
                  className="tabular"
                  style={{
                    fontSize: isTop ? 44 : 26, fontWeight: 800,
                    color: isTop ? 'var(--lime)' : 'var(--blue-deep)',
                  }}
                >
                  {Math.round(item.finalPriorityScore)}
                </div>
              </div>

              {isExpanded && (
                <div style={{ borderTop: isTop ? '1px solid rgba(255,255,255,0.15)' : '1px solid var(--border)', paddingTop: 8, color: isTop ? '#fff' : 'inherit' }}>
                  <div style={{ display: 'grid', gridTemplateColumns: isTop ? '1fr 1fr' : '1fr', gap: '0 32px' }}>
                    <BreakdownRow label="Weakness" value={item.weaknessScore} />
                    <BreakdownRow label="Exam importance" value={item.examImportance} />
                    <BreakdownRow label="Prerequisite impact" value={item.prerequisiteImpact} />
                    <BreakdownRow label="Forgetting risk" value={item.forgettingRisk} />
                    <BreakdownRow label="Difficulty" value={item.difficultyScore} />
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default PriorityPage;