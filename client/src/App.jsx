import { useState, useRef } from 'react';
import GeneratorModal from './GeneratorModal.jsx';

export default function App() {
  const [text, setText] = useState('');
  const [open, setOpen] = useState(false);
  const [toast, setToast] = useState(false);
  const timer = useRef();

  const submit = () => {
    setToast(true);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setToast(false), 3000);
  };

  return (
    <main className="page">
      {toast && <div className="toast" role="status">The campaign has been submitted.</div>}
      <h1>Text Blast</h1>
      <label htmlFor="msg">Campaign message</label>
      <textarea id="msg" rows={7} value={text} onChange={(e) => setText(e.target.value)} placeholder="Type your message or generate one with AI…" />
      <div className="count">{text.length} characters</div>
      <div className="row">
        <button className="btn ghost" onClick={() => setOpen(true)}>✦ AI Content Generator</button>
        <button className="btn primary" onClick={submit} disabled={!text.trim()}>Submit</button>
      </div>
      {open && <GeneratorModal onClose={() => setOpen(false)} onSelect={(v) => { setText(v); setOpen(false); }} />}
    </main>
  );
}
