import { useState, useEffect, useRef } from 'react';
import { api, getCurrentSubjectId } from '../api/client';

function GlobalSearch({ onNavigate }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);
  const [topicsCache, setTopicsCache] = useState(null);
  const boxRef = useRef(null);

  useEffect(() => {
    const subjectId = getCurrentSubjectId();
    if (subjectId) api.getTopicsForSubject(subjectId).then(setTopicsCache).catch(() => setTopicsCache([]));
  }, []);

  useEffect(() => {
    function handleClickOutside(e) {
      if (boxRef.current && !boxRef.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!query.trim() || !topicsCache) { setResults([]); return; }
    const q = query.toLowerCase();
    setResults(topicsCache.filter((t) => t.name.toLowerCase().includes(q)).slice(0, 6));
  }, [query, topicsCache]);

  return (
    <div ref={boxRef} style={{ position: 'relative', width: 280 }}>
      <input
        value={query}
        onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
        onFocus={() => setOpen(true)}
        placeholder="Search topics…"
        style={{ width: '100%', padding: '9px 14px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 13, fontFamily: 'var(--font)' }}
      />
      {open && results.length > 0 && (
        <div style={{ position: 'absolute', top: 42, left: 0, right: 0, background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 10, boxShadow: '0 8px 24px rgba(0,0,0,0.1)', zIndex: 20 }}>
          {results.map((t) => (
            <div
              key={t.id}
              onClick={() => { onNavigate('syllabus'); setOpen(false); setQuery(''); }}
              style={{ padding: '10px 14px', fontSize: 13.5, cursor: 'pointer', borderBottom: '1px solid var(--border)' }}
            >
              {t.name}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default GlobalSearch;