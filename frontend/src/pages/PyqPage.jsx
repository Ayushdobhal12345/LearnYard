import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId } from '../api/client';
import { useToast } from '../context/ToastContext';
import PageHeader from '../components/PageHeader';

function PyqPage() {
  const showToast = useToast();
  const subjectId = getCurrentSubjectId();
  const [papers, setPapers] = useState([]);
  const [ranking, setRanking] = useState([]);
  const [loading, setLoading] = useState(true);
  const [file, setFile] = useState(null);
  const [year, setYear] = useState(new Date().getFullYear());
  const [importing, setImporting] = useState(false);
  const [error, setError] = useState('');

  const loadAll = () => {
    setLoading(true);
    Promise.all([api.getPyqPapers(subjectId), api.getTopicsForSubject(subjectId)])
      .then(async ([papersData, topics]) => {
        setPapers(papersData);
        const scored = await Promise.all(
          topics.map(async (t) => ({
            name: t.name,
            score: (await api.getExamImportance(t.id, subjectId)).examImportanceScore,
          }))
        );
        setRanking(scored.sort((a, b) => b.score - a.score));
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadAll(); }, []);

  const handleImport = (e) => {
    e.preventDefault();
    if (!file) return;
    setImporting(true);
    setError('');
    api.importPyqPaper(file, subjectId, Number(year))
      .then((res) => {
        showToast(`Added ${res.questionsAdded} questions (${res.questionsSkipped} skipped, no topic match).`);
        setFile(null);
        loadAll();
      })
      .catch((err) => setError(err.message))
      .finally(() => setImporting(false));
  };

  if (loading) return <p style={{ color: 'var(--gray)' }}>Loading…</p>;

  return (
    <div>
      <PageHeader eyebrow="Exam history" title="Previous year questions." subtitle="Import old papers — AI maps each question to a topic automatically." />

      {error && <p style={{ fontSize: 13, color: '#c0392b', marginBottom: 16 }}>{error}</p>}

      <form onSubmit={handleImport} style={{ display: 'flex', gap: 12, alignItems: 'center', padding: '20px 24px', background: 'var(--surface)', border: '1px dashed var(--border-strong)', borderRadius: 'var(--radius-lg)', marginBottom: 24 }}>
        <input type="number" value={year} onChange={(e) => setYear(e.target.value)} style={{ width: 90, padding: '10px 12px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14 }} />
        <input type="file" accept=".pdf,.txt,.png,.jpg,.jpeg" onChange={(e) => setFile(e.target.files[0])} style={{ fontSize: 13 }} />
        <button type="submit" disabled={importing || !file} style={{ padding: '10px 20px', fontSize: 13.5, fontWeight: 700, color: 'var(--dark)', background: 'var(--lime)', border: 'none', borderRadius: 10, cursor: 'pointer', opacity: importing || !file ? 0.6 : 1 }}>
          {importing ? 'Reading paper…' : 'Import paper'}
        </button>
      </form>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '22px 24px' }}>
          <div style={{ fontSize: 14, fontWeight: 700, marginBottom: 12 }}>Imported papers</div>
          {papers.length === 0 ? <p style={{ fontSize: 13.5, color: 'var(--gray)' }}>None yet.</p> :
            papers.map((p) => <div key={p.id} style={{ padding: '8px 0', fontSize: 13.5, borderBottom: '1px solid var(--border)' }}>Year {p.year}</div>)}
        </div>

        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '22px 24px' }}>
          <div style={{ fontSize: 14, fontWeight: 700, marginBottom: 12 }}>Exam importance ranking</div>
          {ranking.length === 0 ? <p style={{ fontSize: 13.5, color: 'var(--gray)' }}>Import a paper to see this.</p> :
            ranking.map((r, i) => (
              <div key={r.name} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--border)', fontSize: 13.5 }}>
                <span>{i + 1}. {r.name}</span>
                <span style={{ fontWeight: 700, color: 'var(--blue-deep)' }}>{Math.round(r.score)}</span>
              </div>
            ))}
        </div>
      </div>
    </div>
  );
}

export default PyqPage;