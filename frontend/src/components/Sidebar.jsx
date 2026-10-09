import { useState } from 'react';
import { getCurrentStudent, logout } from '../api/client';

const SECTIONS = [
  {
    label: 'OVERVIEW',
    items: [
      { id: 'dashboard', label: 'Dashboard', icon: 'M3 13h4v7H3zM10 8h4v12h-4zM17 3h4v17h-4z' },
      { id: 'readiness', label: 'Readiness', icon: 'M12 2a10 10 0 100 20 10 10 0 000-20zm-1 15l-4-4 1.4-1.4L11 14.2l5.6-5.6L18 10l-7 7z' },
    ],
  },
  {
    label: 'LEARN',
    items: [
      { id: 'syllabus', label: 'Mastery', icon: 'M4 4h16v3H4zM4 10.5h16v3H4zM4 17h10v3H4z' },
      { id: 'learn', label: 'Learn', icon: 'M12 3L2 9l10 6 10-6-10-6zM2 15l10 6 10-6' },
      { id: 'flashcards', label: 'Flashcards', icon: 'M4 6h16v12H4V6zm2 3h12M6 12h8' },
      { id: 'pyq', label: 'PYQ', icon: 'M4 4h16v16H4V4zm2 4h12M6 12h12M6 16h8' },
      { id: 'graph', label: 'Dependency Map', icon: 'M12 3L2 9l10 6 10-6-10-6zM2 15l10 6 10-6M2 12l10 6 10-6' },
    ],
  },
  {
    label: 'PLAN & PRACTICE',
    items: [
      { id: 'priority', label: 'Priority', icon: 'M12 2l2.4 7.4H22l-6.2 4.5L18.2 21 12 16.3 5.8 21l2.4-7.1L2 9.4h7.6z' },
      { id: 'studyplan', label: 'Study plan', icon: 'M4 5h16v2H4zM4 11h10v2H4zM4 17h13v2H4z' },
      { id: 'quiz', label: 'Quiz', icon: 'M9 17a3 3 0 106 0 3 3 0 00-6 0zM12 3a5 5 0 00-5 5h2a3 3 0 116 0c0 1.7-1.3 2.4-2.2 3.1-.9.7-1.8 1.5-1.8 3.4h2c0-1 .5-1.5 1.4-2.2C15.4 11.5 17 10.3 17 8a5 5 0 00-5-5z' },
      { id: 'viva', label: 'Viva practice', icon: 'M12 14a3 3 0 003-3V5a3 3 0 10-6 0v6a3 3 0 003 3zm5-3a5 5 0 01-10 0H5a7 7 0 006 6.92V21h2v-3.08A7 7 0 0019 11h-2z' },
      { id: 'mistakes', label: 'Mistake notebook', icon: 'M6 2h12a1 1 0 011 1v19l-7-4-7 4V3a1 1 0 011-1z' },
      { id: 'revision', label: 'Revision', icon: 'M12 4a8 8 0 108 8h-2a6 6 0 11-6-6V2L8 6l4 4V4z' },
      { id: 'focus', label: 'Focus timer', icon: 'M6 2h12v5l-4 5 4 5v5H6v-5l4-5-4-5V2z' },
    ],
  },
  {
    label: 'ASSISTANT',
    items: [{ id: 'assistant', label: 'Assistant', icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3a3 3 0 110 6 3 3 0 010-6zm0 14.2a7.2 7.2 0 01-6-3.22c.03-2 4-3.1 6-3.1s5.97 1.1 6 3.1a7.2 7.2 0 01-6 3.22z' }],
  },
  {
    label: 'MANAGE',
    items: [
      { id: 'curriculum', label: 'Curriculum', icon: 'M12 3L2 9l10 6 10-6-10-6zM2 15l10 6 10-6M2 12l10 6 10-6' },
      { id: 'materials', label: 'Materials', icon: 'M4 4h16v16H4V4zm2 4h12M6 12h12M6 16h8' },
    ],
  },
];

function Sidebar({ activeTab, onTabChange }) {
  const [expanded, setExpanded] = useState(true);
  const [menuOpen, setMenuOpen] = useState(false);
  const student = getCurrentStudent();
  const initials = (student?.name || '?').split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase();

  return (
    <aside
      style={{
        width: expanded ? 224 : 84,
        flexShrink: 0,
        background: 'var(--dark)',
        height: '100vh',
        position: 'sticky',
        top: 0,
        display: 'flex',
        flexDirection: 'column',
        transition: 'width 0.18s ease',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: expanded ? 'space-between' : 'center', padding: expanded ? '24px 20px 16px' : '24px 0 16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 38, height: 38, borderRadius: 10, background: 'var(--lime)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 15, fontWeight: 800, color: 'var(--dark)' }}>
            L
          </div>
          {expanded && <span style={{ fontSize: 16, fontWeight: 800, color: '#fff', letterSpacing: '-0.01em' }}>Learnyard</span>}
        </div>
        {expanded && (
          <button onClick={() => setExpanded(false)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'rgba(255,255,255,0.4)' }}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M15 6l-6 6 6 6V6z" /></svg>
          </button>
        )}
      </div>

      {!expanded && (
        <button onClick={() => setExpanded(true)} style={{ width: 48, height: 28, margin: '0 auto 8px', background: 'none', border: 'none', cursor: 'pointer', color: 'rgba(255,255,255,0.35)' }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M9 6l6 6-6 6V6z" /></svg>
        </button>
      )}

      <nav className="no-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: expanded ? '4px 12px' : '4px 0', minHeight: 0 }}>
        {SECTIONS.map((section) => (
          <div key={section.label} style={{ marginBottom: 18 }}>
            {expanded && (
              <div style={{ fontSize: 10, fontWeight: 700, color: 'rgba(255,255,255,0.32)', letterSpacing: '0.06em', padding: '0 10px', marginBottom: 6 }}>
                {section.label}
              </div>
            )}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {section.items.map((item) => {
                const active = activeTab === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => onTabChange(item.id)}
                    title={item.label}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 11,
                      width: expanded ? '100%' : 48, height: 42,
                      margin: expanded ? 0 : '0 auto',
                      borderRadius: 10, border: 'none', cursor: 'pointer',
                      justifyContent: expanded ? 'flex-start' : 'center',
                      padding: expanded ? '0 12px' : 0,
                      background: active ? 'rgba(211,234,118,0.12)' : 'transparent',
                    }}
                  >
                    <svg width="17" height="17" viewBox="0 0 24 24" fill={active ? 'var(--lime)' : 'rgba(255,255,255,0.45)'} style={{ flexShrink: 0 }}>
                      <path d={item.icon} />
                    </svg>
                    {expanded && (
                      <span style={{ fontSize: 13, fontWeight: active ? 600 : 400, color: active ? '#fff' : 'rgba(255,255,255,0.65)' }}>
                        {item.label}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </nav>

      <div style={{ position: 'relative', padding: expanded ? '14px 20px' : '14px 0', display: 'flex', justifyContent: expanded ? 'flex-start' : 'center', borderTop: '1px solid rgba(255,255,255,0.08)' }}>
        {menuOpen && (
          <div style={{ position: 'absolute', bottom: 60, left: expanded ? 20 : 0, background: 'var(--dark-soft)', borderRadius: 12, padding: 8, width: 180, boxShadow: '0 8px 24px rgba(0,0,0,0.3)', zIndex: 10 }}>
            <div style={{ padding: '8px 10px', fontSize: 12, color: 'rgba(255,255,255,0.5)' }}>{student?.email}</div>
            <button onClick={() => { setMenuOpen(false); onTabChange('settings'); }} style={{ width: '100%', textAlign: 'left', padding: '9px 10px', fontSize: 13, color: '#fff', background: 'none', border: 'none', borderRadius: 8, cursor: 'pointer' }}>Settings</button>
            <button onClick={() => { logout(); window.location.reload(); }} style={{ width: '100%', textAlign: 'left', padding: '9px 10px', fontSize: 13, color: '#ff8a8a', background: 'none', border: 'none', borderRadius: 8, cursor: 'pointer' }}>Log out</button>
          </div>
        )}
        <button
          onClick={() => setMenuOpen((v) => !v)}
          style={{ width: 40, height: 40, borderRadius: '50%', background: 'var(--dark-soft)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 700, color: 'var(--lime)', border: 'none', cursor: 'pointer' }}
        >
          {initials}
        </button>
      </div>
    </aside>
  );
}

export default Sidebar;