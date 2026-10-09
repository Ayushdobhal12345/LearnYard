import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId, getCurrentStudentId } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusMessage from '../components/StatusMessage';
import Markdown from '../components/Markdown';

const LETTERS = ['A', 'B', 'C', 'D'];
const PRACTICE_LIMIT = 10;

const card = {
  background: 'var(--surface)',
  border: '1px solid var(--border)',
  borderRadius: 'var(--radius-lg)',
  padding: '24px 28px',
};

const primaryBtn = {
  padding: '10px 18px', fontSize: 13.5, fontWeight: 700, borderRadius: 10,
  border: 'none', background: 'var(--lime)', color: 'var(--dark)', cursor: 'pointer',
};

const ghostBtn = {
  padding: '10px 18px', fontSize: 13.5, fontWeight: 700, borderRadius: 10,
  border: '1px solid var(--border-strong)', background: '#fff', color: 'var(--dark)', cursor: 'pointer',
};

function optionText(mistake, letter) {
  return mistake[`option${letter}`];
}

function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString();
}

function MistakeCard({ mistake }) {
  return (
    <div style={{ padding: '18px 0', borderTop: '1px solid var(--border)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, marginBottom: 12 }}>
        <div style={{ fontSize: 15.5, fontWeight: 700, lineHeight: 1.45 }}>
          <Markdown text={mistake.questionText} inline />
        </div>
        {mistake.fixed && (
          <span style={{ fontSize: 11.5, fontWeight: 800, color: '#3f7d1f', background: '#e6f3d9', borderRadius: 99, padding: '4px 10px', height: 'fit-content', whiteSpace: 'nowrap' }}>
            Fixed ✓
          </span>
        )}
      </div>

      <div style={{ display: 'grid', gap: 6 }}>
        {LETTERS.map((letter) => {
          const isCorrect = mistake.correctOption === letter;
          const isYours = mistake.yourOption === letter && !isCorrect;
          return (
            <div
              key={letter}
              style={{
                display: 'flex', gap: 10, fontSize: 14, lineHeight: 1.5, padding: '8px 12px', borderRadius: 10,
                background: isCorrect ? '#e6f3d9' : isYours ? '#fbe5e1' : 'transparent',
                border: `1px solid ${isCorrect ? '#bfe0a3' : isYours ? '#f0b9b0' : 'var(--border)'}`,
              }}
            >
              <b style={{ width: 16 }}>{letter}</b>
              <span style={{ flex: 1 }}><Markdown text={optionText(mistake, letter)} inline /></span>
              {isCorrect && <span style={{ fontSize: 12, fontWeight: 700, color: '#3f7d1f' }}>Correct answer</span>}
              {isYours && <span style={{ fontSize: 12, fontWeight: 700, color: '#c0392b' }}>You chose this</span>}
            </div>
          );
        })}
      </div>

      <div style={{ fontSize: 12.5, color: 'var(--gray)', marginTop: 10 }}>
        Wrong {mistake.timesWrong} time{mistake.timesWrong === 1 ? '' : 's'}
        {formatDate(mistake.lastWrongAt) && ` · last on ${formatDate(mistake.lastWrongAt)}`}
      </div>
    </div>
  );
}

function PracticeView({ title, items, onDone }) {
  const [answers, setAnswers] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const allAnswered = items.every((m) => answers[m.questionId]);

  const handleSubmit = () => {
    setSubmitting(true);
    setError('');
    api.submitQuiz({
      studentId: getCurrentStudentId(),
      answers: items.map((m) => ({ questionId: m.questionId, selectedOption: answers[m.questionId] })),
    })
      .then(setResult)
      .catch((err) => setError(err.message))
      .finally(() => setSubmitting(false));
  };

  if (result) {
    const right = items.filter((m) => answers[m.questionId] === m.correctOption).length;
    return (
      <div style={{ display: 'grid', gap: 18, maxWidth: 820 }}>
        <div style={card}>
          <div className="tabular" style={{ fontSize: 52, fontWeight: 800, letterSpacing: '-0.03em', lineHeight: 1 }}>
            {right}/{items.length}
          </div>
          <div style={{ fontSize: 15, marginTop: 8 }}>
            {right === items.length ? 'All fixed. These are off your list.' : 'The ones you missed again stay on your list.'}
            {result.replan && result.replan.planRegenerated ? ' Your study plan was updated too.' : ''}
          </div>
        </div>
        {items.map((m) => {
          const correct = answers[m.questionId] === m.correctOption;
          return (
            <div key={m.questionId} style={{ ...card, padding: '18px 24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
                <div style={{ fontWeight: 700, lineHeight: 1.45 }}><Markdown text={m.questionText} inline /></div>
                <b style={{ color: correct ? '#3f7d1f' : '#c0392b', whiteSpace: 'nowrap' }}>{correct ? 'Correct' : 'Wrong'}</b>
              </div>
              {!correct && (
                <div style={{ fontSize: 14, marginTop: 8, lineHeight: 1.5 }}>
                  Right answer: <b>{m.correctOption}</b>. <Markdown text={optionText(m, m.correctOption)} inline />
                </div>
              )}
            </div>
          );
        })}
        <div><button style={primaryBtn} onClick={onDone}>Back to notebook</button></div>
      </div>
    );
  }

  return (
    <div style={{ display: 'grid', gap: 18, maxWidth: 820 }}>
      <div style={{ fontSize: 14, color: 'var(--gray)' }}>
        Practising: <b style={{ color: 'var(--dark)' }}>{title}</b> · {items.length} question{items.length === 1 ? '' : 's'}
      </div>
      {items.map((m, i) => (
        <div key={m.questionId} style={{ ...card, padding: '20px 24px' }}>
          <div style={{ fontSize: 15.5, fontWeight: 700, lineHeight: 1.45, marginBottom: 12 }}>
            {i + 1}. <Markdown text={m.questionText} inline />
          </div>
          <div style={{ display: 'grid', gap: 6 }}>
            {LETTERS.map((letter) => {
              const chosen = answers[m.questionId] === letter;
              return (
                <button
                  key={letter}
                  onClick={() => setAnswers((prev) => ({ ...prev, [m.questionId]: letter }))}
                  style={{
                    display: 'flex', gap: 10, textAlign: 'left', fontSize: 14, lineHeight: 1.5, padding: '10px 14px',
                    borderRadius: 10, cursor: 'pointer', fontFamily: 'var(--font)',
                    background: chosen ? 'var(--dark)' : '#fff',
                    color: chosen ? '#fff' : 'var(--dark)',
                    border: `1px solid ${chosen ? 'var(--dark)' : 'var(--border-strong)'}`,
                  }}
                >
                  <b style={{ width: 16 }}>{letter}</b>
                  <span><Markdown text={optionText(m, letter)} inline /></span>
                </button>
              );
            })}
          </div>
        </div>
      ))}
      <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
        <button
          style={{ ...primaryBtn, opacity: allAnswered && !submitting ? 1 : 0.5, cursor: allAnswered && !submitting ? 'pointer' : 'not-allowed' }}
          disabled={!allAnswered || submitting}
          onClick={handleSubmit}
        >
          {submitting ? 'Checking…' : 'Check my answers'}
        </button>
        <button style={ghostBtn} onClick={onDone}>Cancel</button>
        {error && <span style={{ fontSize: 13, color: '#c0392b' }}>{error}</span>}
      </div>
    </div>
  );
}

function MistakesPage() {
  const studentId = getCurrentStudentId();
  const subjectId = getCurrentSubjectId();

  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [reload, setReload] = useState(0);
  const [showFixed, setShowFixed] = useState(false);
  const [practice, setPractice] = useState(null);

  useEffect(() => {
    api.getMistakes(studentId, subjectId)
      .then(setData)
      .catch((err) => setError(err.message));
  }, [studentId, subjectId, reload]);

  if (error) return <StatusMessage loading={false} error={error} />;
  if (!data) return <StatusMessage loading error="" />;

  const finishPractice = () => {
    setPractice(null);
    setReload((n) => n + 1);
  };

  if (practice) {
    return (
      <div>
        <PageHeader eyebrow="Mistakes" title="Fix your mistakes." subtitle="Answer these again. Get one right and it drops off your list." />
        <PracticeView title={practice.title} items={practice.items} onDone={finishPractice} />
      </div>
    );
  }

  const openAll = data.topics.flatMap((t) => t.mistakes.filter((m) => !m.fixed));

  return (
    <div>
      <PageHeader
        eyebrow="Mistakes"
        title="Mistake notebook."
        subtitle="Every quiz question you got wrong, grouped by topic. Fix them and they drop off the list."
        action={openAll.length > 0 && (
          <button style={primaryBtn} onClick={() => setPractice({ title: 'all open mistakes', items: openAll.slice(0, PRACTICE_LIMIT) })}>
            Practise {Math.min(openAll.length, PRACTICE_LIMIT)} open mistake{openAll.length === 1 ? '' : 's'}
          </button>
        )}
      />

      <div style={{ display: 'flex', gap: 14, marginBottom: 24, flexWrap: 'wrap' }}>
        <div style={{ ...card, padding: '16px 22px' }}>
          <div className="tabular" style={{ fontSize: 30, fontWeight: 800 }}>{data.openCount}</div>
          <div style={{ fontSize: 12.5, color: 'var(--gray)' }}>open mistakes</div>
        </div>
        <div style={{ ...card, padding: '16px 22px' }}>
          <div className="tabular" style={{ fontSize: 30, fontWeight: 800, color: '#3f7d1f' }}>{data.fixedCount}</div>
          <div style={{ fontSize: 12.5, color: 'var(--gray)' }}>fixed</div>
        </div>
        {data.fixedCount > 0 && (
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13.5, marginLeft: 6, cursor: 'pointer' }}>
            <input type="checkbox" checked={showFixed} onChange={(e) => setShowFixed(e.target.checked)} />
            Show fixed ones
          </label>
        )}
      </div>

      {data.topics.length === 0 ? (
        <div style={card}>
          <div style={{ fontSize: 17, fontWeight: 800, marginBottom: 6 }}>No mistakes yet.</div>
          <div style={{ fontSize: 14.5, color: 'var(--gray)', lineHeight: 1.6 }}>
            Take a quiz. Any question you get wrong is saved here automatically, so you can come back and fix it.
          </div>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 18 }}>
          {data.topics.map((topic) => {
            const visible = topic.mistakes.filter((m) => showFixed || !m.fixed);
            if (visible.length === 0) return null;
            const open = topic.mistakes.filter((m) => !m.fixed);
            return (
              <div key={topic.topicId} style={card}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
                  <div>
                    <div style={{ fontSize: 19, fontWeight: 800 }}>{topic.topicName}</div>
                    <div style={{ fontSize: 13, color: 'var(--gray)', marginTop: 2 }}>{topic.openCount} open</div>
                  </div>
                  {open.length > 0 && (
                    <button style={ghostBtn} onClick={() => setPractice({ title: topic.topicName, items: open.slice(0, PRACTICE_LIMIT) })}>
                      Practise this topic
                    </button>
                  )}
                </div>
                <div style={{ marginTop: 12 }}>
                  {visible.map((m) => <MistakeCard key={m.questionId} mistake={m} />)}
                </div>
              </div>
            );
          })}
          {data.openCount === 0 && !showFixed && (
            <div style={card}>
              <div style={{ fontSize: 17, fontWeight: 800, marginBottom: 6 }}>Nothing left to fix.</div>
              <div style={{ fontSize: 14.5, color: 'var(--gray)' }}>Every mistake you made has since been answered correctly.</div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default MistakesPage;
