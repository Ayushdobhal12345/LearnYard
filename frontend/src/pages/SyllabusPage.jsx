import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { api, getCurrentStudentId, getCurrentSubjectId } from '../api/client';
import StatusMessage from '../components/StatusMessage';
import PageHeader from '../components/PageHeader';

function masteryColor(score) {
  if (score >= 70) return 'var(--green)';
  if (score >= 45) return 'var(--cyan)';
  return 'var(--primary)';
}

function MasteryBar({ score }) {
  const color = masteryColor(score);
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, width: 180 }}>
      <div style={{ flex: 1, height: 6, background: 'var(--ink-100)', borderRadius: 3 }}>
        <div style={{ width: `${score}%`, height: '100%', background: color, borderRadius: 3 }} />
      </div>
      <span className="tabular" style={{ fontSize: 13, fontWeight: 600, color, width: 38, textAlign: 'right' }}>
        {Math.round(score)}%
      </span>
    </div>
  );
}

function TrendChart({ topicId, studentId }) {
  const [history, setHistory] = useState(null);

  useEffect(() => {
    api.getMasteryHistory(studentId, topicId).then(setHistory).catch(() => setHistory([]));
  }, [topicId]);

  if (history === null) return <p style={{ fontSize: 13, color: 'var(--gray)' }}>Loading trend…</p>;
  if (history.length < 2) return <p style={{ fontSize: 13, color: 'var(--gray)' }}>Not enough data yet — take a few more quizzes to see a trend.</p>;

  const chartData = history.map((h) => ({
    date: new Date(h.recordedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
    mastery: Math.round(h.masteryScore),
  }));

  return (
    <div style={{ height: 180, marginTop: 4 }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={chartData}>
          <XAxis dataKey="date" tick={{ fontSize: 11 }} />
          <YAxis domain={[0, 100]} tick={{ fontSize: 11 }} />
          <Tooltip />
          <Line type="monotone" dataKey="mastery" stroke="var(--dark)" strokeWidth={2} dot={{ r: 3 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

function SyllabusPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedTopicId, setExpandedTopicId] = useState(null);

  useEffect(() => {
    Promise.all([api.getTopicsForSubject(getCurrentSubjectId()), api.getMasteryForStudent(getCurrentStudentId())])
      .then(([topics, mastery]) => {
        const masteryByTopicId = new Map(mastery.map((m) => [m.topicId, m]));
        setRows(
          topics.map((topic) => ({
            topicId: topic.id,
            topicName: topic.name,
            unitName: topic.unitName,
            masteryScore: masteryByTopicId.get(topic.id)?.masteryScore ?? null,
          }))
        );
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading || error) return <StatusMessage loading={loading} error={error} />;

  return (
    <div>
      <PageHeader eyebrow="Syllabus" title="What you know, and what's next." subtitle="Click a topic to see its mastery trend over time." />

      <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '4px 26px' }}>
        {rows.map((row, i) => {
          const isExpanded = expandedTopicId === row.topicId;
          return (
            <div key={row.topicId} style={{ borderBottom: i < rows.length - 1 ? '1px solid var(--border)' : 'none' }}>
              <div
                onClick={() => setExpandedTopicId(isExpanded ? null : row.topicId)}
                style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px 0', cursor: 'pointer' }}
              >
                <div>
                  <div style={{ fontSize: 14.5, fontWeight: 600 }}>{row.topicName}</div>
                  <div style={{ fontSize: 12, color: 'var(--gray)', marginTop: 2 }}>{row.unitName}</div>
                </div>
                {row.masteryScore === null ? (
                  <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--gray)' }}>Not yet assessed</span>
                ) : (
                  <MasteryBar score={row.masteryScore} />
                )}
              </div>
              {isExpanded && row.masteryScore !== null && (
                <div style={{ paddingBottom: 16 }}>
                  <TrendChart topicId={row.topicId} studentId={getCurrentStudentId()} />
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default SyllabusPage;