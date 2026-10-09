/*
 * Renders the Markdown that AI models produce (**bold**, *italic*, headings, bullet and
 * numbered lists, tables, code) as clean text. It builds React elements directly and never
 * uses innerHTML, so AI output cannot inject HTML or scripts.
 *
 *   <Markdown text={reply} />            full blocks (lessons, chat replies)
 *   <Markdown text={sentence} inline />  one line of formatted text (cards, options, feedback)
 */

const INLINE = /(\*\*[^*\n]+\*\*|`[^`\n]+`|\*[^*\s][^*\n]*?\*)/g;

function renderInline(text, keyBase) {
  const out = [];
  let last = 0;
  let n = 0;
  const source = text || '';

  for (const match of source.matchAll(INLINE)) {
    if (match.index > last) out.push(clean(source.slice(last, match.index)));
    const token = match[0];
    const key = `${keyBase}-${n}`;
    n += 1;
    if (token.startsWith('**')) {
      out.push(<strong key={key}>{renderInline(token.slice(2, -2), key)}</strong>);
    } else if (token.startsWith('`')) {
      out.push(
        <code
          key={key}
          style={{ background: 'rgba(0,0,0,0.07)', borderRadius: 5, padding: '1px 6px', fontSize: '0.92em', fontFamily: 'ui-monospace, Menlo, Consolas, monospace' }}
        >
          {token.slice(1, -1)}
        </code>
      );
    } else {
      out.push(<em key={key}>{token.slice(1, -1)}</em>);
    }
    last = match.index + token.length;
  }
  if (last < source.length) out.push(clean(source.slice(last)));
  return out;
}

// Removes markdown leftovers (unpaired ** or stray bullets) that would otherwise show as raw symbols.
function clean(text) {
  return text.replace(/\*\*/g, '');
}

function splitRow(line) {
  return line.trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map((c) => c.trim());
}

const isSeparatorRow = (line) => /^\s*\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)*\|?\s*$/.test(line);
const BULLET = /^(\s*)([*\-•])\s+(.*)$/;
const NUMBERED = /^(\s*)(\d+)[.)]\s+(.*)$/;
const HEADING = /^(#{1,6})\s+(.*)$/;
const RULE = /^\s*([-*_])(\s*\1){2,}\s*$/;

function isBlockStart(line) {
  return !line.trim() || HEADING.test(line) || BULLET.test(line) || NUMBERED.test(line)
    || line.trim().startsWith('```') || line.trim().startsWith('>') || line.trim().startsWith('|') || RULE.test(line);
}

function Markdown({ text, inline = false }) {
  if (!text) return null;

  if (inline) {
    // Flatten to one formatted line: drop heading/bullet markers, keep bold/italic/code.
    const flat = text
      .replace(/^#{1,6}\s+/gm, '')
      .replace(/^\s*[*\-•]\s+/gm, '')
      .replace(/\n+/g, ' ')
      .trim();
    return <>{renderInline(flat, 'i')}</>;
  }

  const lines = text.replace(/\r\n/g, '\n').split('\n');
  const blocks = [];
  let i = 0;

  while (i < lines.length) {
    const line = lines[i];
    const key = `b${blocks.length}`;

    if (!line.trim()) {
      i += 1;
    } else if (line.trim().startsWith('```')) {
      const code = [];
      i += 1;
      while (i < lines.length && !lines[i].trim().startsWith('```')) {
        code.push(lines[i]);
        i += 1;
      }
      i += 1;
      blocks.push(
        <pre key={key} style={{ background: 'var(--dark)', color: '#f3f1ea', borderRadius: 12, padding: '14px 16px', overflowX: 'auto', fontSize: 13, lineHeight: 1.55, margin: '0 0 12px' }}>
          <code>{code.join('\n')}</code>
        </pre>
      );
    } else if (HEADING.test(line)) {
      const [, hashes, title] = line.match(HEADING);
      const big = hashes.length <= 2;
      blocks.push(
        <div key={key} style={{ fontSize: big ? '1.18em' : '1.05em', fontWeight: 800, margin: '18px 0 8px', letterSpacing: '-0.01em' }}>
          {renderInline(title.replace(/\*\*/g, ''), key)}
        </div>
      );
      i += 1;
    } else if (RULE.test(line)) {
      blocks.push(<hr key={key} style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '14px 0' }} />);
      i += 1;
    } else if (line.trim().startsWith('|') && i + 1 < lines.length && isSeparatorRow(lines[i + 1])) {
      const header = splitRow(line);
      const rows = [];
      i += 2;
      while (i < lines.length && lines[i].trim().startsWith('|')) {
        rows.push(splitRow(lines[i]));
        i += 1;
      }
      blocks.push(
        <div key={key} style={{ overflowX: 'auto', margin: '0 0 14px' }}>
          <table style={{ borderCollapse: 'collapse', fontSize: '0.95em', minWidth: '60%' }}>
            <thead>
              <tr>
                {header.map((h, c) => (
                  <th key={c} style={{ textAlign: 'left', padding: '8px 12px', borderBottom: '2px solid var(--border-strong)', fontWeight: 700 }}>
                    {renderInline(h, `${key}h${c}`)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((r, ri) => (
                <tr key={ri}>
                  {r.map((cell, c) => (
                    <td key={c} style={{ padding: '8px 12px', borderBottom: '1px solid var(--border)', verticalAlign: 'top' }}>
                      {renderInline(cell, `${key}r${ri}c${c}`)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    } else if (BULLET.test(line) || NUMBERED.test(line)) {
      const ordered = NUMBERED.test(line) && !BULLET.test(line);
      const items = [];
      while (i < lines.length && (BULLET.test(lines[i]) || NUMBERED.test(lines[i]))) {
        const m = lines[i].match(BULLET) || lines[i].match(NUMBERED);
        items.push({ level: Math.min(3, Math.floor(m[1].length / 2)), text: m[3] });
        i += 1;
      }
      const ListTag = ordered ? 'ol' : 'ul';
      blocks.push(
        <ListTag key={key} style={{ margin: '0 0 12px', paddingLeft: 22 }}>
          {items.map((item, idx) => (
            <li key={idx} style={{ marginBottom: 5, marginLeft: item.level * 16, lineHeight: 'inherit' }}>
              {renderInline(item.text, `${key}l${idx}`)}
            </li>
          ))}
        </ListTag>
      );
    } else if (line.trim().startsWith('>')) {
      const quote = [];
      while (i < lines.length && lines[i].trim().startsWith('>')) {
        quote.push(lines[i].replace(/^\s*>\s?/, ''));
        i += 1;
      }
      blocks.push(
        <div key={key} style={{ borderLeft: '3px solid var(--lime-deep)', paddingLeft: 14, margin: '0 0 12px', color: '#444' }}>
          {renderInline(quote.join(' '), key)}
        </div>
      );
    } else {
      const para = [];
      while (i < lines.length && (para.length === 0 || !isBlockStart(lines[i]))) {
        para.push(lines[i]);
        i += 1;
      }
      blocks.push(
        <p key={key} style={{ margin: '0 0 10px', lineHeight: 'inherit' }}>
          {para.map((p, idx) => (
            <span key={idx}>
              {idx > 0 && <br />}
              {renderInline(p, `${key}p${idx}`)}
            </span>
          ))}
        </p>
      );
    }
  }

  return <div className="md-body">{blocks}</div>;
}

export default Markdown;
