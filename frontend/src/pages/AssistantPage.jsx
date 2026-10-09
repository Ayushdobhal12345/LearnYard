import { useState, useRef, useEffect } from 'react';
import { api, getCurrentStudentId, getCurrentSubjectId } from '../api/client';
import PageHeader from '../components/PageHeader';
import Markdown from '../components/Markdown';

function groupByDate(messages) {
  const groups = [];
  let lastDate = null;
  for (const m of messages) {
    const date = m.createdAt ? new Date(m.createdAt).toDateString() : null;
    if (date && date !== lastDate) {
      groups.push({ type: 'date', label: date });
      lastDate = date;
    }
    groups.push({ type: 'message', ...m });
  }
  return groups;
}

function AssistantPage() {
  const [messages, setMessages] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const scrollRef = useRef(null);

  const loadHistory = () => {
    setLoadingHistory(true);
    api.getChatHistory(getCurrentStudentId())
      .then((history) => setMessages(history))
      .catch(() => setMessages([]))
      .finally(() => setLoadingHistory(false));
  };

  useEffect(() => { loadHistory(); }, []);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = (e) => {
    e.preventDefault();
    const trimmed = input.trim();
    if (!trimmed || sending) return;

    setMessages((prev) => [...prev, { role: 'user', text: trimmed, createdAt: new Date().toISOString() }]);
    setInput('');
    setError('');
    setSending(true);

    api.sendChatMessage(getCurrentStudentId(), trimmed, getCurrentSubjectId())
      .then((res) => {
        setMessages((prev) => [...prev, { role: 'assistant', text: res.reply, createdAt: new Date().toISOString() }]);
      })
      .catch((err) => {
        setError(err.message);
        setMessages((prev) => [...prev, { role: 'assistant', text: "Sorry, I couldn't process that. Please try again." }]);
      })
      .finally(() => setSending(false));
  };

  const handleNewChat = () => {
    if (!window.confirm('Start a new conversation? This clears your chat history permanently.')) return;
    api.clearChatHistory(getCurrentStudentId()).then(() => {
      setMessages([]);
    });
  };

  const grouped = groupByDate(messages);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 96px)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <PageHeader eyebrow="AI Assistant" title="Ask anything." subtitle="Grounded in your real data — never guessed." />
        <button
          onClick={handleNewChat}
          style={{ padding: '9px 16px', fontSize: 12.5, fontWeight: 700, background: 'none', border: '1px solid var(--border-strong)', borderRadius: 10, cursor: 'pointer', marginBottom: 20 }}
        >
          New chat
        </button>
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 }}>
        {['What should I study next?', 'Show my weak topics', 'Explain my notes'].map((q) => (
          <button
            key={q}
            onClick={() => setInput(q)}
            style={{ fontSize: 12.5, padding: '7px 14px', borderRadius: 999, border: '1px solid var(--border-strong)', background: 'var(--surface)', cursor: 'pointer' }}
          >
            {q}
          </button>
        ))}
      </div>

      <div
        style={{
          flex: 1, overflowY: 'auto', background: 'var(--surface)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)', padding: '28px 30px', marginBottom: 18,
          display: 'flex', flexDirection: 'column', gap: 14,
        }}
      >
        {loadingHistory ? (
          <p style={{ color: 'var(--gray)', fontSize: 13 }}>Loading conversation…</p>
        ) : grouped.length === 0 ? (
          <p style={{ color: 'var(--gray)', fontSize: 14 }}>Hi! Ask me anything — your mastery, priorities, study plan, your notes, or general questions.</p>
        ) : (
          grouped.map((item, i) =>
            item.type === 'date' ? (
              <div key={i} style={{ textAlign: 'center', fontSize: 11, color: 'var(--gray)', fontWeight: 700, letterSpacing: '0.04em', margin: '6px 0' }}>
                {item.label}
              </div>
            ) : (
              <div key={i} style={{ alignSelf: item.role === 'user' ? 'flex-end' : 'flex-start', maxWidth: '75%' }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: 'var(--gray)', marginBottom: 6, textAlign: item.role === 'user' ? 'right' : 'left' }}>
                  {item.role === 'user' ? 'YOU' : 'ASSISTANT'}
                </div>
                <div
                  style={{
                    padding: '14px 18px', borderRadius: 14, fontSize: 14.5, lineHeight: 1.6, whiteSpace: item.role === 'user' ? 'pre-wrap' : 'normal',
                    background: item.role === 'user' ? 'var(--dark)' : 'var(--bg)',
                    color: item.role === 'user' ? '#fff' : 'var(--dark)',
                    border: item.role === 'user' ? 'none' : '1px solid var(--border)',
                  }}
                >
                  {item.role === 'user' ? item.text : <Markdown text={item.text} />}
                </div>
              </div>
            )
          )
        )}
        {sending && <div style={{ fontSize: 13, color: 'var(--gray)', fontStyle: 'italic' }}>Thinking… this can take 10–20 seconds.</div>}
        <div ref={scrollRef} />
      </div>

      {error && <p style={{ fontSize: 12.5, color: 'var(--gray)', marginTop: -10, marginBottom: 10 }}>{error}</p>}

      <form onSubmit={handleSend} style={{ display: 'flex', gap: 12 }}>
        <input
          type="text" value={input} onChange={(e) => setInput(e.target.value)}
          placeholder="Ask about a topic, your plan, or your notes..." disabled={sending}
          style={{ flex: 1, padding: '14px 18px', fontSize: 14.5, fontFamily: 'var(--font)', border: '1px solid var(--border-strong)', borderRadius: 12 }}
        />
        <button
          type="submit" disabled={sending || !input.trim()}
          style={{ padding: '14px 26px', fontSize: 14, fontWeight: 700, color: 'var(--dark)', background: 'var(--lime)', border: 'none', borderRadius: 12, cursor: sending ? 'default' : 'pointer', opacity: sending || !input.trim() ? 0.6 : 1 }}
        >
          Send
        </button>
      </form>
    </div>
  );
}

export default AssistantPage;