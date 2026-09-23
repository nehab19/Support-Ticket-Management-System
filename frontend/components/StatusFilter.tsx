import React from 'react';
import type { TicketStatus } from '../lib/types';

const STATUS_OPTIONS: Array<{ value: TicketStatus | ''; label: string }> = [
  { value: '', label: 'All statuses' },
  { value: 'OPEN', label: 'Open' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'CLOSED', label: 'Closed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

interface Props {
  value: TicketStatus | '';
  onStatusChange: (status: TicketStatus | '') => void;
}

export default function StatusFilter({ value, onStatusChange }: Props) {
  return (
    <select
      value={value}
      onChange={(e) => onStatusChange(e.target.value as TicketStatus | '')}
      aria-label="Filter by status"
      style={{
        padding: '8px 12px',
        border: '1px solid #d1d5db',
        borderRadius: '6px',
        fontSize: '14px',
        background: '#fff',
      }}
    >
      {STATUS_OPTIONS.map((opt) => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>
  );
}
