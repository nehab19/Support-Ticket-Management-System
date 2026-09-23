import React from 'react';
import { render, screen, within } from '@testing-library/react';
import TicketTable from '../components/TicketTable';
import type { TicketSummary } from '../lib/types';

// Next.js Link requires a router — mock it simply
jest.mock('next/link', () => {
  const MockLink = ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  );
  MockLink.displayName = 'Link';
  return MockLink;
});

const makeTicket = (overrides: Partial<TicketSummary> = {}): TicketSummary => ({
  id: 1,
  title: 'Default Title',
  priority: 'MEDIUM',
  status: 'OPEN',
  assignee: null,
  createdAt: '2024-01-15T10:00:00Z',
  ...overrides,
});

const SAMPLE_TICKETS: TicketSummary[] = [
  makeTicket({ id: 1, title: 'First ticket', priority: 'HIGH', status: 'OPEN', createdAt: '2024-01-01T08:00:00Z' }),
  makeTicket({ id: 2, title: 'Second ticket', priority: 'LOW', status: 'IN_PROGRESS', createdAt: '2024-02-10T09:00:00Z' }),
  makeTicket({ id: 3, title: 'Third ticket', priority: 'CRITICAL', status: 'RESOLVED', createdAt: '2024-03-20T12:00:00Z' }),
];

describe('TicketTable', () => {
  it('renders a message when there are no tickets', () => {
    render(<TicketTable tickets={[]} />);
    expect(screen.getByText(/no tickets found/i)).toBeInTheDocument();
  });

  it('renders the correct number of rows', () => {
    render(<TicketTable tickets={SAMPLE_TICKETS} />);
    const rows = screen.getAllByRole('row');
    // +1 for the header row
    expect(rows).toHaveLength(SAMPLE_TICKETS.length + 1);
  });

  it('displays the ticket title in each row', () => {
    render(<TicketTable tickets={SAMPLE_TICKETS} />);
    expect(screen.getByText('First ticket')).toBeInTheDocument();
    expect(screen.getByText('Second ticket')).toBeInTheDocument();
    expect(screen.getByText('Third ticket')).toBeInTheDocument();
  });

  it('displays the priority value in each row', () => {
    render(<TicketTable tickets={SAMPLE_TICKETS} />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('LOW')).toBeInTheDocument();
    expect(screen.getByText('CRITICAL')).toBeInTheDocument();
  });

  it('displays the status value in each row', () => {
    render(<TicketTable tickets={SAMPLE_TICKETS} />);
    // Status is rendered with underscores replaced by spaces
    expect(screen.getByText('OPEN')).toBeInTheDocument();
    expect(screen.getByText('IN PROGRESS')).toBeInTheDocument();
    expect(screen.getByText('RESOLVED')).toBeInTheDocument();
  });

  it('displays a formatted createdAt date in each row', () => {
    render(<TicketTable tickets={[makeTicket({ createdAt: '2024-01-15T10:00:00Z' })]} />);
    // new Date().toLocaleDateString() — just check the cell exists and is non-empty
    const rows = screen.getAllByRole('row');
    // First data row (index 1)
    const cells = within(rows[1]).getAllByRole('cell');
    // Created column is 4th cell (index 3)
    expect(cells[3].textContent).not.toBe('');
  });

  it('links each title to the correct ticket detail page', () => {
    render(<TicketTable tickets={[makeTicket({ id: 42, title: 'Linked ticket' })]} />);
    const link = screen.getByRole('link', { name: 'Linked ticket' });
    expect(link).toHaveAttribute('href', '/tickets/42');
  });
});
