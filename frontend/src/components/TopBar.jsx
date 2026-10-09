function TopBar({ title }) {
  return (
    <header
      style={{
        height: 64,
        borderBottom: '1px solid var(--border)',
        background: 'var(--surface)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 32px',
        position: 'sticky',
        top: 0,
        zIndex: 5,
      }}
    >
      <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--ink-900)' }}>{title}</div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div
          style={{
            display: 'flex', alignItems: 'center', gap: 8,
            background: 'var(--ink-100)', borderRadius: 999, padding: '8px 16px', width: 260,
          }}
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="var(--ink-500)" strokeWidth="2">
            <circle cx="11" cy="11" r="7" />
            <path d="M21 21l-4.3-4.3" />
          </svg>
          <span style={{ fontSize: 13, color: 'var(--ink-500)' }}>Search topics...</span>
        </div>

        <span
          style={{
            fontSize: 12, fontWeight: 700, color: 'var(--green)',
            background: 'var(--green-soft)', padding: '6px 13px', borderRadius: 999,
          }}
        >
          Active
        </span>

        <div
          style={{
            width: 34, height: 34, borderRadius: '50%', background: 'var(--primary)',
            color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 13, fontWeight: 700,
          }}
        >
          RS
        </div>
      </div>
    </header>
  );
}

export default TopBar;