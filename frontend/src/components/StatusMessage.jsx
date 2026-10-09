function StatusMessage({ loading, error }) {
  if (loading) {
    return <p style={{ color: 'var(--ink-500)', fontSize: 14, padding: '8px 0' }}>Loading…</p>;
  }
  if (error) {
    return (
      <p
        style={{
          color: 'var(--danger)',
          background: 'var(--danger-soft)',
          fontSize: 14,
          padding: '10px 14px',
          borderRadius: 'var(--radius)',
        }}
      >
        {error}
      </p>
    );
  }
  return null;
}

export default StatusMessage;