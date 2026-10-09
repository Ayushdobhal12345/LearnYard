import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId, getCurrentStudentId } from '../api/client';
import PageHeader from '../components/PageHeader';

function masteryColor(score) {
  if (score === null) return '#9ca3af';
  if (score >= 70) return '#2e7d4f';
  if (score >= 45) return '#4c8bc2';
  return '#c0392b';
}

function GraphPage() {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const subjectId = getCurrentSubjectId();
    const studentId = getCurrentStudentId();

    Promise.all([api.getTopicsForSubject(subjectId), api.getMasteryForStudent(studentId)])
      .then(async ([topics, mastery]) => {
        const masteryMap = new Map(mastery.map((m) => [m.topicId, m.masteryScore]));

        const edgeList = [];
        const dependentCount = new Map();
        const prereqCount = new Map();
        topics.forEach((t) => { dependentCount.set(t.id, 0); prereqCount.set(t.id, 0); });

        await Promise.all(topics.map(async (t) => {
          const deps = await api.getPrerequisitesOf(t.id);
          deps.forEach((d) => {
            if (topics.some((x) => x.id === d.prerequisiteTopicId)) {
              edgeList.push({ from: d.prerequisiteTopicId, to: d.topicId });
              dependentCount.set(d.prerequisiteTopicId, (dependentCount.get(d.prerequisiteTopicId) || 0) + 1);
              prereqCount.set(t.id, (prereqCount.get(t.id) || 0) + 1);
            }
          });
        }));

        // Level = longest chain of prerequisites before this topic (simple leveling)
        const levels = new Map();
        const remaining = new Set(topics.map((t) => t.id));
        let currentLevel = 0;
        while (remaining.size > 0) {
          const ready = [...remaining].filter((id) => {
            const deps = edgeList.filter((e) => e.to === id).map((e) => e.from);
            return deps.every((d) => levels.has(d) || !remaining.has(d));
          });
          if (ready.length === 0) { [...remaining].forEach((id) => levels.set(id, currentLevel)); break; }
          ready.forEach((id) => { levels.set(id, currentLevel); remaining.delete(id); });
          currentLevel++;
        }

        const columnWidth = 200;
        const rowHeight = 90;
        const byLevel = {};
        topics.forEach((t) => {
          const lvl = levels.get(t.id) ?? 0;
          byLevel[lvl] = byLevel[lvl] || [];
          byLevel[lvl].push(t);
        });

        const positioned = topics.map((t) => {
          const lvl = levels.get(t.id) ?? 0;
          const indexInLevel = byLevel[lvl].findIndex((x) => x.id === t.id);
          return {
            ...t,
            x: 90 + lvl * columnWidth,
            y: 60 + indexInLevel * rowHeight,
            mastery: masteryMap.get(t.id) ?? null,
            blocks: dependentCount.get(t.id) || 0,
          };
        });

        setNodes(positioned);
        setEdges(edgeList);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p style={{ color: 'var(--gray)' }}>Loading…</p>;

  const findNode = (id) => nodes.find((n) => n.id === id);
  const maxY = Math.max(300, ...nodes.map((n) => n.y + 80));
  const maxX = Math.max(600, ...nodes.map((n) => n.x + 180));

  return (
    <div>
      <PageHeader
        eyebrow="Structure"
        title="Learning order & bottlenecks."
        subtitle="Left to right = the order to study in. A bold badge means many other topics depend on it — falling behind here slows everything after it."
      />

      {nodes.length === 0 ? (
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: 40, textAlign: 'center' }}>
          <p style={{ color: 'var(--gray)', fontSize: 14 }}>No topics in this subject yet. Add topics in Curriculum first.</p>
        </div>
      ) : edges.length === 0 ? (
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: 40, textAlign: 'center' }}>
          <p style={{ color: 'var(--gray)', fontSize: 14 }}>
            No prerequisite relationships defined yet for this subject's topics.
            Add dependencies via the API to see the learning order here.
          </p>
        </div>
      ) : (
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: 24, overflow: 'auto' }}>
          <svg width={maxX} height={maxY}>
            <defs>
              <marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
                <path d="M0,0 L10,5 L0,10 z" fill="var(--gray)" />
              </marker>
            </defs>
            {edges.map((e, i) => {
              const from = findNode(e.from);
              const to = findNode(e.to);
              if (!from || !to) return null;
              return (
                <line key={i} x1={from.x + 80} y1={from.y + 20} x2={to.x} y2={to.y + 20}
                  stroke="var(--gray)" strokeWidth="1.6" markerEnd="url(#arrow)" opacity="0.55" />
              );
            })}
            {nodes.map((n) => (
              <g key={n.id}>
                <rect x={n.x} y={n.y} width="160" height="40" rx="10" fill="var(--dark)" stroke={masteryColor(n.mastery)} strokeWidth="2.5" />
                <text x={n.x + 12} y={n.y + 24} fill="#fff" fontSize="12" fontWeight="700">
                  {n.name.length > 18 ? n.name.slice(0, 17) + '…' : n.name}
                </text>
                {n.blocks > 0 && (
                  <g>
                    <circle cx={n.x + 160} cy={n.y} r="11" fill="var(--lime)" />
                    <text x={n.x + 160} y={n.y + 4} textAnchor="middle" fontSize="11" fontWeight="800" fill="var(--dark)">
                      {n.blocks}
                    </text>
                  </g>
                )}
              </g>
            ))}
          </svg>
          <div style={{ display: 'flex', gap: 20, marginTop: 16, fontSize: 12, color: 'var(--gray)' }}>
            <span><span style={{ display: 'inline-block', width: 10, height: 10, borderRadius: 3, background: '#2e7d4f', marginRight: 6 }} />Strong mastery</span>
            <span><span style={{ display: 'inline-block', width: 10, height: 10, borderRadius: 3, background: '#4c8bc2', marginRight: 6 }} />Developing</span>
            <span><span style={{ display: 'inline-block', width: 10, height: 10, borderRadius: 3, background: '#c0392b', marginRight: 6 }} />Weak</span>
            <span><span style={{ display: 'inline-block', width: 10, height: 10, borderRadius: 999, background: 'var(--lime)', marginRight: 6 }} />Number of topics this one blocks</span>
          </div>
        </div>
      )}
    </div>
  );
}

export default GraphPage;