import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId } from '../api/client';
import PageHeader from '../components/PageHeader';
import Markdown from '../components/Markdown';

function FlashcardsPage() {
  const [topics, setTopics] = useState([]);
  const [selectedTopicId, setSelectedTopicId] = useState('');
  const [cards, setCards] = useState([]);
  const [index, setIndex] = useState(0);
  const [flipped, setFlipped] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getTopicsForSubject(getCurrentSubjectId()).then((t) => {
      setTopics(t);
      if (t.length > 0) setSelectedTopicId(String(t[0].id));
    });
  }, []);

  const handleGenerate = (refresh = false) => {
    setLoading(true);
    setError('');
    setCards([]);
    api.getFlashcards(selectedTopicId, 8, refresh)
      .then((res) => { setCards(res); setIndex(0); setFlipped(false); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  const next = () => { setFlipped(false); setIndex((i) => Math.min(i + 1, cards.length - 1)); };
  const prev = () => { setFlipped(false); setIndex((i) => Math.max(i - 1, 0)); };

  return (
    <div>
      <PageHeader eyebrow="Review" title="Flashcards." subtitle="AI-generated, grounded in your notes when available." />

      <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
        <select value={selectedTopicId} onChange={(e) => setSelectedTopicId(e.target.value)} style={{ padding: '10px 14px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14 }}>
          {topics.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
        </select>
        <button onClick={() => handleGenerate(false)} disabled={loading} style={{ padding: '10px 22px', fontSize: 13.5, fontWeight: 700, color: 'var(--dark)', background: 'var(--lime)', border: 'none', borderRadius: 10, cursor: 'pointer', opacity: loading ? 0.6 : 1 }}>
          {loading ? 'Generating…' : 'Generate 8 flashcards'}
        </button>
        {cards.length > 0 && (
          <button onClick={() => handleGenerate(true)} disabled={loading} style={{ padding: '10px 18px', fontSize: 13.5, fontWeight: 700, color: 'var(--dark)', background: '#fff', border: '1px solid var(--border-strong)', borderRadius: 10, cursor: 'pointer', opacity: loading ? 0.6 : 1 }}>
            New set
          </button>
        )}
      </div>

      {error && <p style={{ fontSize: 13, color: '#c0392b', marginBottom: 16 }}>{error}</p>}

      {cards.length > 0 && (
        <div>
          <div
            onClick={() => setFlipped((f) => !f)}
            style={{
              background: flipped ? 'var(--dark)' : 'var(--surface)',
              color: flipped ? '#fff' : 'var(--dark)',
              border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)',
              padding: '60px 40px', minHeight: 220, display: 'flex', alignItems: 'center', justifyContent: 'center',
              textAlign: 'center', fontSize: 17, fontWeight: 600, cursor: 'pointer', marginBottom: 16,
            }}
          >
            <Markdown text={flipped ? cards[index].back : cards[index].front} inline />
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <button onClick={prev} disabled={index === 0} style={{ padding: '9px 18px', borderRadius: 10, border: '1px solid var(--border-strong)', background: 'none', cursor: 'pointer' }}>← Prev</button>
            <span style={{ fontSize: 13, color: 'var(--gray)' }}>{index + 1} / {cards.length} — click card to flip</span>
            <button onClick={next} disabled={index === cards.length - 1} style={{ padding: '9px 18px', borderRadius: 10, border: '1px solid var(--border-strong)', background: 'none', cursor: 'pointer' }}>Next →</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default FlashcardsPage;