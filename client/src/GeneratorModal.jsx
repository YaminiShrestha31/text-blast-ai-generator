import { useState } from 'react';

const TYPES = ['Promotional', 'Reminder', 'Offer', 'Announcement', 'Event', 'Re-engagement', 'Other'];
const TONES = ['Friendly', 'Professional', 'Energetic'];

export default function GeneratorModal({ onClose, onSelect }) {
  const [type, setType] = useState('Promotional');
  const [tone, setTone] = useState('Friendly');
  const [limit, setLimit] = useState(true);
  const [emoji, setEmoji] = useState(true);
  const [prompt, setPrompt] = useState(''); // always a string
  const [variants, setVariants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const generate = async () => {
    setLoading(true); setError(''); setVariants([]);
    try {
      const r = await fetch('/api/generate', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, campaignType: type, tone, limit, emoji }),
      });
      const data = await r.json();
      if (!r.ok) throw new Error(data.error || 'Something went wrong.');
      setVariants(data.variants);
    } catch (e) { setError(e.message); } finally { setLoading(false); }
  };

  const Chip = ({ active, onClick, children }) => (
    <button type="button" className={`chip ${active ? 'on' : ''}`} onClick={onClick}>{children}</button>
  );
  const Toggle = ({ on, set, label }) => (
    <label className="toggle"><input type="checkbox" checked={on} onChange={(e) => set(e.target.checked)} /><span className="sw" />{label}</label>
  );

  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
        <header>
          <div><h2>✦ AI Content Generator</h2><p>Tune the brief on the left, compare variants on the right</p></div>
          <button className="x" onClick={onClose} aria-label="Close">×</button>
        </header>
        <div className="body">
          <section className="left">
            <h4>Campaign type</h4>
            <div className="chips">{TYPES.map((t) => <Chip key={t} active={type === t} onClick={() => setType(t)}>{t}</Chip>)}</div>
            <h4>Tone</h4>
            <div className="chips">{TONES.map((t) => <Chip key={t} active={tone === t} onClick={() => setTone(t)}>{t}</Chip>)}</div>
            <h4>Length &amp; style</h4>
            <Toggle on={limit} set={setLimit} label="Limit to 9 segments (1600 chars)" />
            {!limit && <p className="warn">⚠ Longer messages use more SMS segments and will increase your SMS cost.</p>}
            <Toggle on={emoji} set={setEmoji} label="Include emoji" />
            <h4>Prompt</h4>
            <textarea rows={4} value={prompt} onChange={(e) => setPrompt(e.target.value)}
              placeholder="e.g. Promotional SMS for Black Friday sale with 25% off internal painting services" />
            <button className="btn primary full" onClick={generate} disabled={loading || !prompt.trim()}>
              {loading ? 'Generating…' : '✦ Generate 3 variants'}
            </button>
            {error && <p className="err">{error}</p>}
          </section>
          <section className="right">
            {loading && <p className="hint">Writing your variants…</p>}
            {!loading && !variants.length && <p className="hint">Set your options and prompt, then generate 3 variants to compare side by side.</p>}
            {variants.map((v, i) => (
              <article className="variant" key={i}>
                <div className="vtext"><strong>Variant {i + 1}</strong><p>{v}</p><small>{v.length} characters</small></div>
                <button className="btn primary" onClick={() => onSelect(v)}>Select</button>
              </article>
            ))}
          </section>
        </div>
      </div>
    </div>
  );
}
