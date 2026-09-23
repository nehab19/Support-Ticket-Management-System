import React, { useState } from 'react';
import * as api from '../lib/api';
import type { Ticket, Priority, ErrorResponse } from '../lib/types';
import ErrorNotification from './ErrorNotification';

interface Props {
  onCreated: (ticket: Ticket) => void;
  onClose: () => void;
}

const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function CreateTicketForm({ onCreated, onClose }: Props) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const ticket = await api.createTicket({ title, description, priority });
      onCreated(ticket);
    } catch (err) {
      const apiErr = err as ErrorResponse;
      setError(apiErr.message || 'Failed to create ticket');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={overlayStyle}>
      <div style={modalStyle}>
        <h2 style={{ marginBottom: '16px' }}>New Ticket</h2>
        <ErrorNotification error={error} />
        <form onSubmit={handleSubmit}>
          <label style={labelStyle}>
            Title *
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              maxLength={255}
              style={inputStyle}
            />
          </label>
          <label style={labelStyle}>
            Description *
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              rows={4}
              style={{ ...inputStyle, resize: 'vertical' }}
            />
          </label>
          <label style={labelStyle}>
            Priority
            <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)} style={inputStyle}>
              {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </label>
          <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '16px' }}>
            <button type="button" onClick={onClose} style={btnSecondaryStyle}>Cancel</button>
            <button type="submit" disabled={submitting} style={btnPrimaryStyle}>
              {submitting ? 'Creating...' : 'Create Ticket'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

const overlayStyle: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
};
const modalStyle: React.CSSProperties = {
  background: '#fff', borderRadius: '8px', padding: '24px',
  width: '480px', maxWidth: '90vw', boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
};
const labelStyle: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: '4px', marginBottom: '12px',
  fontSize: '14px', fontWeight: 500, color: '#374151',
};
const inputStyle: React.CSSProperties = {
  padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: '6px',
  fontSize: '14px', width: '100%',
};
const btnPrimaryStyle: React.CSSProperties = {
  background: '#2563eb', color: '#fff', border: 'none', borderRadius: '6px',
  padding: '8px 20px', cursor: 'pointer', fontSize: '14px',
};
const btnSecondaryStyle: React.CSSProperties = {
  background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: '6px',
  padding: '8px 20px', cursor: 'pointer', fontSize: '14px',
};
