import { useState, useEffect } from 'react';
import { api, getCurrentStudentId, getCurrentSubjectId } from '../api/client';
import PageHeader from '../components/PageHeader';
import Markdown from '../components/Markdown';

function LearnPage() {
  const [topics, setTopics] = useState([]);
  const [selectedTopicId, setSelectedTopicId] = useState('');
  const [lesson, setLesson] = useState('');
  const [fromSaved, setFromSaved] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getTopicsForSubject(getCurrentSubjectId()).then((t) => {
      setTopics(t);
      if (t.length > 0) setSelectedTopicId(String(t[0].id));
    });
  }, []);

  const handleTeach = (refresh = false) => {
    if (!selectedTopicId) return;
    setLoading(true);
    setError('');
    setLesson('');
    api.getLesson(getCurrentStudentId(), selectedTopicId, refresh)
      .then((res) => { setLesson(res.lesson); setFromSaved(Boolean(res.cached)); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  return (
    <div>
      <PageHeader eyebrow="Learn" title="Have the AI teach you." subtitle="A structured lesson, adjusted to your current mastery level." />

      <div style={{ display: 'flex', gap: 12, marginBottom: 20 }}>
        <select
          value={selectedTopicId} onChange={(e) => setSelectedTopicId(e.target.value)}
          style={{ padding: '10px 14px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14 }}
        >
          {topics.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
        </select>
        <button
          onClick={() => handleTeach(false)} disabled={loading}
          style={{ padding: '10px 22px', fontSize: 13.5, fontWeight: 700, color: 'var(--dark)', background: 'var(--lime)', border: 'none', borderRadius: 10, cursor: 'pointer', opacity: loading ? 0.6 : 1 }}
        >
          {loading ? 'Preparing lesson…' : 'Teach me this topic'}
        </button>
      </div>

      {error && <p style={{ fontSize: 13, color: '#c0392b', marginBottom: 16 }}>{error}</p>}

      {lesson && (
        <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '30px 34px' }}>
          <div style={{ fontSize: 14.5, lineHeight: 1.7 }}>
            <Markdown text={lesson} />
          </div>
          {fromSaved && (
            <div style={{ marginTop: 22, paddingTop: 14, borderTop: '1px solid var(--border)', fontSize: 12.5, color: 'var(--gray)' }}>
              Saved lesson.{' '}
              <button
                onClick={() => handleTeach(true)}
                disabled={loading}
                style={{ background: 'none', border: 'none', padding: 0, fontSize: 12.5, fontWeight: 700, color: 'var(--dark)', cursor: 'pointer', textDecoration: 'underline' }}
              >
                Write a new one
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default LearnPage;