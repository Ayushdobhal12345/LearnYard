import '../styles/landing.css';

const FEATURES = [
  {
    title: 'Live mastery tracking',
    text: 'Every quiz updates a mastery score for each topic, so you always know what is solid and what is slipping.',
    icon: 'M3 13h4v7H3zM10 8h4v12h-4zM17 3h4v17h-4z',
  },
  {
    title: 'Prerequisite map',
    text: 'See how topics connect. AI can detect prerequisites for you so you learn things in the right order.',
    icon: 'M12 3L2 9l10 6 10-6-10-6zM2 15l10 6 10-6M2 12l10 6 10-6',
  },
  {
    title: 'Lessons from your notes',
    text: 'Upload your own study material and get lessons and flashcards grounded in it, not generic answers.',
    icon: 'M4 4h16v16H4V4zm2 4h12M6 12h12M6 16h8',
  },
  {
    title: 'Adaptive quizzes',
    text: 'Generate practice questions for any topic and get instant, scored feedback that feeds your mastery.',
    icon: 'M9 17a3 3 0 106 0 3 3 0 00-6 0zM12 3a5 5 0 00-5 5h2a3 3 0 116 0c0 1.7-1.3 2.4-2.2 3.1-.9.7-1.8 1.5-1.8 3.4h2c0-1 .5-1.5 1.4-2.2C15.4 11.5 17 10.3 17 8a5 5 0 00-5-5z',
  },
  {
    title: 'Smart study plan',
    text: 'Your available hours are spread across topics by priority, so the weakest, most important topics come first.',
    icon: 'M4 5h16v2H4zM4 11h10v2H4zM4 17h13v2H4z',
  },
  {
    title: 'Flashcards and revision',
    text: 'AI flashcards plus a revision schedule that brings topics back before you forget them.',
    icon: 'M12 4a8 8 0 108 8h-2a6 6 0 11-6-6V2L8 6l4 4V4z',
  },
  {
    title: 'Previous-year papers',
    text: 'Add past papers and see which topics show up most often, so you spend time where marks are.',
    icon: 'M12 2l2.4 7.4H22l-6.2 4.5L18.2 21 12 16.3 5.8 21l2.4-7.1L2 9.4h7.6z',
  },
  {
    title: 'AI study assistant',
    text: 'Ask questions in plain language. The assistant knows your syllabus and how you are progressing.',
    icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3a3 3 0 110 6 3 3 0 010-6zm0 14.2a7.2 7.2 0 01-6-3.22c.03-2 4-3.1 6-3.1s5.97 1.1 6 3.1a7.2 7.2 0 01-6 3.22z',
  },
  {
    title: 'Focus timer and streaks',
    text: 'Study in focused blocks on a topic. Every finished session is logged and keeps your streak alive.',
    icon: 'M6 2h12v5l-4 5 4 5v5H6v-5l4-5-4-5V2z',
  },
];

const STEPS = [
  {
    title: 'Add your curriculum',
    text: 'Create a subject with units and topics, or import a syllabus file and let AI structure it.',
  },
  {
    title: 'Upload your notes',
    text: 'Attach study material to topics so lessons and flashcards use your own content.',
  },
  {
    title: 'Practice and track',
    text: 'Take quizzes, review flashcards and run focus sessions. Mastery updates as you go.',
  },
  {
    title: 'Follow your plan',
    text: 'Get a prioritised study plan and revision reminders based on what you actually know.',
  },
];

function LandingPage({ onLogin, onSignup }) {
  return (
    <div className="ly-page">
      {/* ---------- Navbar ---------- */}
      <header className="ly-nav">
        <div className="ly-wrap ly-nav-inner">
          <a className="ly-brand" href="#top">
            <span className="ly-mark">L</span>
            LearnYard
          </a>
          <nav className="ly-links">
            <a href="#about">About</a>
            <a href="#features">Features</a>
            <a href="#how">How it works</a>
            <a href="#assistant">AI assistant</a>
          </nav>
          <div className="ly-actions">
            <button className="ly-btn ly-btn-ghost" onClick={onLogin}>Log in</button>
            <button className="ly-btn ly-btn-primary" onClick={onSignup}>Sign up</button>
          </div>
        </div>
      </header>

      {/* ---------- Hero ---------- */}
      <section className="ly-hero" id="top">
        <div className="ly-wrap ly-hero-grid">
          <div>
            <span className="ly-eyebrow">For students who study to understand</span>
            <h1 className="ly-h1">Learn smarter.<br />Master <em>every</em> topic.</h1>
            <p className="ly-lead">
              Learnyard turns your syllabus into a personal learning path. It tracks what you know,
              finds what you are missing, and plans your time around it.
            </p>
            <div className="ly-cta-row">
              <button className="ly-btn ly-btn-primary ly-btn-lg" onClick={onSignup}>Get started free</button>
              <button className="ly-btn ly-btn-ghost ly-btn-lg" onClick={onLogin}>I already have an account</button>
            </div>
            <div className="ly-chips">
              <span className="ly-chip">Mastery tracking</span>
              <span className="ly-chip">AI lessons</span>
              <span className="ly-chip">Study plans</span>
              <span className="ly-chip">Flashcards</span>
            </div>
          </div>

          <div className="ly-preview" aria-hidden="true">
            <span className="ly-preview-tag">Preview</span>
            <h4>Overall mastery</h4>
            <div className="ly-preview-score">72%<span>+8 this week</span></div>
            <div className="ly-bar-row">
              <div className="ly-bar-label"><span>Data structures</span><span>88%</span></div>
              <div className="ly-bar"><i style={{ width: '88%' }} /></div>
            </div>
            <div className="ly-bar-row">
              <div className="ly-bar-label"><span>Databases</span><span>64%</span></div>
              <div className="ly-bar"><i className="blue" style={{ width: '64%' }} /></div>
            </div>
            <div className="ly-bar-row">
              <div className="ly-bar-label"><span>Operating systems</span><span>41%</span></div>
              <div className="ly-bar"><i className="warn" style={{ width: '41%' }} /></div>
            </div>
            <div className="ly-preview-foot">
              <div className="ly-pill"><b>6</b><span>day streak</span></div>
              <div className="ly-pill"><b>3</b><span>due for revision</span></div>
            </div>
          </div>
        </div>
      </section>

      {/* ---------- About ---------- */}
      <section className="ly-section" id="about">
        <div className="ly-wrap">
          <div className="ly-section-head">
            <div className="ly-kicker">About Learnyard</div>
            <h2 className="ly-h2">Studying is easier when you can see where you stand.</h2>
            <p className="ly-sub">
              Most students revise what feels familiar and skip what is hard. Learnyard measures your
              understanding topic by topic, then helps you spend your hours where they matter. It is
              built for college and exam preparation, and it works with your own syllabus and notes.
            </p>
          </div>
        </div>
      </section>

      {/* ---------- Features ---------- */}
      <section className="ly-section" id="features" style={{ paddingTop: 0 }}>
        <div className="ly-wrap">
          <div className="ly-section-head">
            <div className="ly-kicker">Features</div>
            <h2 className="ly-h2">Everything you need, in one place.</h2>
            <p className="ly-sub">From the first topic you add to the last revision before your exam.</p>
          </div>
          <div className="ly-grid3">
            {FEATURES.map((f) => (
              <article className="ly-card" key={f.title}>
                <div className="ly-icon">
                  <svg viewBox="0 0 24 24"><path d={f.icon} /></svg>
                </div>
                <h3>{f.title}</h3>
                <p>{f.text}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- How it works ---------- */}
      <section className="ly-section" id="how" style={{ paddingTop: 0 }}>
        <div className="ly-wrap">
          <div className="ly-section-head">
            <div className="ly-kicker">How it works</div>
            <h2 className="ly-h2">From syllabus to strategy in four steps.</h2>
          </div>
          <div className="ly-steps">
            {STEPS.map((s, i) => (
              <div className="ly-step" key={s.title}>
                <div className="ly-step-num">{i + 1}</div>
                <h3>{s.title}</h3>
                <p>{s.text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- AI assistant ---------- */}
      <section className="ly-section" id="assistant" style={{ paddingTop: 0 }}>
        <div className="ly-wrap">
          <div className="ly-ai">
            <div>
              <div className="ly-kicker">AI-assisted learning</div>
              <h2 className="ly-h2">An assistant that knows your syllabus.</h2>
              <p className="ly-sub">
                Ask what to revise, get a topic explained, or turn your notes into practice questions.
                Answers are based on your subjects, your materials and your progress.
              </p>
              <ul>
                <li>Lessons and flashcards grounded in your uploaded notes</li>
                <li>Quiz questions generated for any topic</li>
                <li>Prerequisites detected automatically from your topic list</li>
              </ul>
            </div>
            <div className="ly-chat" aria-hidden="true">
              <div className="ly-bubble user">What should I revise before Friday?</div>
              <div className="ly-bubble bot">
                Start with Normalization and Transactions. Both are below 50% mastery, and Joins is due for revision.
              </div>
              <div className="ly-bubble user">Make me 5 questions on Normalization.</div>
              <div className="ly-chat-note">Illustrative example</div>
            </div>
          </div>
        </div>
      </section>

      {/* ---------- Final CTA ---------- */}
      <section className="ly-section" style={{ paddingTop: 0 }}>
        <div className="ly-wrap">
          <div className="ly-cta">
            <h2>Ready to study with a plan?</h2>
            <p>Create your free account and add your first subject in a couple of minutes.</p>
            <div className="ly-cta-row" style={{ justifyContent: 'center' }}>
              <button className="ly-btn ly-btn-dark ly-btn-lg" onClick={onSignup}>Create your account</button>
              <button className="ly-btn ly-btn-ghost ly-btn-lg" onClick={onLogin}>Log in</button>
            </div>
          </div>
        </div>
      </section>

      {/* ---------- Footer ---------- */}
      <footer className="ly-footer">
        <div className="ly-wrap">
          <div className="ly-footer-grid">
            <div>
              <a className="ly-brand" href="#top">
                <span className="ly-mark">L</span>
                LearnYard
              </a>
              <p>Learn smarter, one topic at a time.</p>
            </div>
            <div>
              <h5>Explore</h5>
              <a href="#about">About</a>
              <a href="#features">Features</a>
              <a href="#how">How it works</a>
            </div>
            <div>
              <h5>Account</h5>
              <button className="ly-link" onClick={onLogin}>Log in</button>
              <button className="ly-link" onClick={onSignup}>Sign up</button>
            </div>
          </div>
          <div className="ly-copy">© 2026 Learnyard. All rights reserved.</div>
        </div>
      </footer>
    </div>
  );
}

export default LandingPage;
