function PageHeader({ eyebrow, title, subtitle, action }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: 36 }}>
      <div>
        {eyebrow && (
          <div style={{ fontSize: 12.5, fontWeight: 700, color: 'var(--gray)', letterSpacing: '0.08em', marginBottom: 10, textTransform: 'uppercase' }}>
            {eyebrow}
          </div>
        )}
        <h1 style={{ fontSize: 42, fontWeight: 800, lineHeight: 1.05, margin: 0, letterSpacing: '-0.02em' }}>
          {title}
        </h1>
        {subtitle && (
          <p style={{ fontSize: 15, color: 'var(--gray)', marginTop: 10, maxWidth: 480, lineHeight: 1.5 }}>
            {subtitle}
          </p>
        )}
      </div>
      {action}
    </div>
  );
}

export default PageHeader;