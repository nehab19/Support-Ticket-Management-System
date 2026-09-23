import React, { useState } from 'react';
import * as api from '../lib/api';
import type { Ticket, Priority, ErrorResponse } from '../lib/types';
import ErrorNotification from './ErrorNotification';

interface Props {
  ticket: Ticket;
  onUpdated: (ticket: Ticket) => void;
}

const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function UpdateTicketForm({ ticket, onUpdated }: Props) {
  const [title, setTitle] = useState(ticket.title);
  const [description, setDescription] = useState(ticket.description);
  const [priority, setPriority] = useState<Priority>(ticket.priority);
  const [assignee, setAssignee] = useState(ticket.assignee ?? '');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const payload: Record<string, string> = {};
      if (title !== ticket.title) payload.title = title;
      if (description !== ticket.description) payload.description = description;
      if (priority !== ticket.priority) payload.priority = priority;
      if (assignee !== (ticket.assignee ?? '')) payload.assignee = assignee;

      const updated = await api.updateTicket(ticket.id, payload);
      onUpdated(updated);
    } catch (err) {
      const apiErr = err as ErrorResponse;
      setError(apiErr.message || 'Failed to update ticket');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ background: '#f9fafb', borderRadius: '8px', padding: '16px' }}>
      <h3 style={{ marginBottom: '12px', fontSize: '16px' }}>Edit Ticket</h3>
      <ErrorNotification error={error} />
      <label style={labelStyle}>
        Title
        <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={255} style={inputStyle} />
      </label>
      <label style={labelStyle}>
        Description
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} style={{ ...inputStyle, resize: 'vertical' }} />
      </label>
      <label style={labelStyle}>
        Priority
        <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)} style={inputStyle}>
          {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
        </select>
      </label>
      <label style={labelStyle}>
        Assignee
        <input value={assignee} onChange={(e) => setAssignee(e.target.value)} placeholder="Unassigned" style={inputStyle} />
      </label>
      <button type="submit" disabled={submitting} style={btnStyle}>
        {submitting ? 'Saving...' : 'Save Changes'}
      </button>
    </form>
  );
}

const labelStyle: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: '4px',
  marginBottom: '12px', fontSize: '14px', fontWeight: 500, color: '#374151',
};
const inputStyle: React.CSSProperties = {
  padding: '8px 12px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '14px', width: '100%',
};
const btnStyle: React.CSSProperties = {
  background: '#2563eb', color: '#fff', border: 'none',
  borderRadius: '6px', padding: '8px 20px', cursor: 'pointer', fontSize: '14px',
};
