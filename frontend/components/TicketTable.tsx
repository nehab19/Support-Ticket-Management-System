import React from 'react';
import Link from 'next/link';
import type { TicketSummary } from '../lib/types';

interface Props {
  tickets: TicketSummary[];
}

const PRIORITY_COLORS: Record<string, string> = {
  LOW: '#6b7280',
  MEDIUM: '#2563eb',
  HIGH: '#d97706',
  CRITICAL: '#dc2626',
};

const STATUS_COLORS: Record<string, string> = {
  OPEN: '#059669',
  IN_PROGRESS: '#7c3aed',
  RESOLVED: '#2563eb',
  CLOSED: '#6b7280',
  CANCELLED: '#ef4444',
};

export default function TicketTable({ tickets }: Props) {
  if (tickets.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: '#6b7280', padding: '32px' }}>
        No tickets found.
      </p>
    );
  }

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: '8px', overflow: 'hidden' }}>
      <thead>
        <tr style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
          <th style={thStyle}>Title</th>
          <th style={thStyle}>Priority</th>
          <th style={thStyle}>Status</th>
          <th style={thStyle}>Created</th>
          <th style={thStyle}>Assignee</th>
        </tr>
      </thead>
      <tbody>
        {tickets.map((ticket) => (
          <tr key={ticket.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
            <td style={tdStyle}>
              <Link href={`/tickets/${ticket.id}`} style={{ color: '#2563eb', fontWeight: 500 }}>
                {ticket.title}
              </Link>
            </td>
            <td style={tdStyle}>
              <span style={{ color: PRIORITY_COLORS[ticket.priority], fontWeight: 500 }}>
                {ticket.priority}
              </span>
            </td>
            <td style={tdStyle}>
              <span style={{
                background: STATUS_COLORS[ticket.status] + '20',
                color: STATUS_COLORS[ticket.status],
                borderRadius: '4px',
                padding: '2px 8px',
                fontSize: '12px',
                fontWeight: 600,
              }}>
                {ticket.status.replace('_', ' ')}
              </span>
            </td>
            <td style={tdStyle}>
              {new Date(ticket.createdAt).toLocaleDateString()}
            </td>
            <td style={tdStyle}>{ticket.assignee ?? '—'}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

const thStyle: React.CSSProperties = {
  padding: '12px 16px', textAlign: 'left', fontSize: '13px',
  fontWeight: 600, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em',
};

const tdStyle: React.CSSProperties = {
  padding: '12px 16px', fontSize: '14px', color: '#374151',
};
