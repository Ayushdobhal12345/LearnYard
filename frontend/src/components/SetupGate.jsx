/*
 * Wraps a page that needs something to exist before it can work.
 *   need="curriculum"  the subject needs at least one topic
 *   need="notes"       the subject needs topics, and the student is asked to upload notes
 * When it is missing, the student gets a short explanation and a button that takes them to the
 * right place, instead of a page full of errors.
 */

import { useState } from 'react';

// Remembers "continue without notes" choices for the rest of the browser session.
const skippedNotes = new Set();

const wrap = {
  maxWidth: 600,
  margin: '40px auto',
  background: 'var(--surface)',
  border: '1px solid var(--border)',
  borderRadius: 'var(--radius-lg)',
  padding: '40px 40px 34px',
  textAlign: 'center',
};

const primary = {
  padding: '13px 26px', fontSize: 14.5, fontWeight: 700, borderRadius: 11,
  border: 'none', background: 'var(--lime)', color: 'var(--dark)', cursor: 'pointer',
};

const secondary = {
  padding: '13px 22px', fontSize: 14.5, fontWeight: 700, borderRadius: 11,
  border: '1px solid var(--border-strong)', background: '#fff', color: 'var(--dark)', cursor: 'pointer',
};

function Step({ number, label, done, current }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <span
        style={{
          width: 24, height: 24, borderRadius: '50%', fontSize: 12, fontWeight: 800,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: done ? 'var(--lime)' : current ? 'var(--dark)' : 'var(--border)',
          color: done ? 'var(--dark)' : current ? 'var(--lime)' : 'var(--gray)',
        }}
      >
        {done ? '✓' : number}
      </span>
      <span style={{ fontSize: 13, fontWeight: current ? 700 : 600, color: current ? 'var(--dark)' : 'var(--gray)' }}>
        {label}
      </span>
    </div>
  );
}

function Guide({ title, text, hasCurriculum, hasNotes, children }) {
  return (
    <div style={wrap}>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 22, flexWrap: 'wrap', marginBottom: 28 }}>
        <Step number={1} label="Add curriculum" done={hasCurriculum} current={!hasCurriculum} />
        <Step number={2} label="Upload notes" done={hasNotes} current={hasCurriculum && !hasNotes} />
        <Step number={3} label="Start studying" done={false} current={false} />
      </div>
      <h2 style={{ fontSize: 26, fontWeight: 800, letterSpacing: '-0.01em', margin: '0 0 10px' }}>{title}</h2>
      <p style={{ fontSize: 15, lineHeight: 1.65, color: 'var(--gray)', margin: '0 auto 26px', maxWidth: 440 }}>{text}</p>
      <div style={{ display: 'flex', gap: 10, justifyContent: 'center', flexWrap: 'wrap' }}>{children}</div>
    </div>
  );
}

function SetupGate({ need, feature, status, onNavigate, children }) {
  const [, forceRender] = useState(0);

  if (!status) {
    return <p style={{ color: 'var(--gray)', fontSize: 14, padding: '8px 0' }}>Loading…</p>;
  }
  if (status.unavailable) {
    return children;
  }

  const hasCurriculum = status.topicCount > 0;
  const hasNotes = status.materialCount > 0;

  if (!hasCurriculum) {
    return (
      <Guide
        title="Add your curriculum first"
        text={`${feature} works on your topics. Add the units and topics you are studying, or import a syllabus file and let AI organise it for you.`}
        hasCurriculum={false}
        hasNotes={hasNotes}
      >
        <button style={primary} onClick={() => onNavigate('curriculum')}>Go to Curriculum</button>
      </Guide>
    );
  }

  if (need === 'notes' && !hasNotes && !skippedNotes.has(feature)) {
    return (
      <Guide
        title="Upload your notes first"
        text={`${feature} is at its best when it can use your own study material. Upload notes for a topic and it will teach from them. You can also carry on without notes.`}
        hasCurriculum
        hasNotes={false}
      >
        <button style={primary} onClick={() => onNavigate('materials')}>Upload notes</button>
        <button
          style={secondary}
          onClick={() => {
            skippedNotes.add(feature);
            forceRender((n) => n + 1);
          }}
        >
          Continue without notes
        </button>
      </Guide>
    );
  }

  return children;
}

export default SetupGate;
