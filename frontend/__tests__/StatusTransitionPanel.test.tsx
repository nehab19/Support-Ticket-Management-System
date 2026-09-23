import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import StatusTransitionPanel from '../components/StatusTransitionPanel';
import type { Ticket, ErrorResponse } from '../lib/types';

// Mock the api module so we don't make real HTTP calls
jest.mock('../lib/api', () => ({
  transitionStatus: jest.fn(),
}));

import * as api from '../lib/api';
const mockTransitionStatus = api.transitionStatus as jest.MockedFunction<typeof api.transitionStatus>;

const makeTicket = (status: Ticket['status']): Ticket => ({
  id: 1,
  title: 'Test ticket',
  description: 'Description',
  priority: 'MEDIUM',
  status,
  assignee: null,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
  comments: [],
});

describe('StatusTransitionPanel — valid transition buttons', () => {
  it('shows IN_PROGRESS and CANCELLED buttons for OPEN status', () => {
    render(<StatusTransitionPanel ticket={makeTicket('OPEN')} onTransitioned={jest.fn()} />);
    expect(screen.getByRole('button', { name: /transition to in progress/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /transition to cancelled/i })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /transition to closed/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /transition to resolved/i })).toBeNull();
  });

  it('shows no buttons for CLOSED status', () => {
    render(<StatusTransitionPanel ticket={makeTicket('CLOSED')} onTransitioned={jest.fn()} />);
    expect(screen.queryByRole('button')).toBeNull();
    expect(screen.getByText(/no further transitions available/i)).toBeInTheDocument();
  });

  it('shows RESOLVED and CANCELLED buttons for IN_PROGRESS status', () => {
    render(<StatusTransitionPanel ticket={makeTicket('IN_PROGRESS')} onTransitioned={jest.fn()} />);
    expect(screen.getByRole('button', { name: /transition to resolved/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /transition to cancelled/i })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /transition to open/i })).toBeNull();
  });

  it('shows CLOSED button for RESOLVED status', () => {
    render(<StatusTransitionPanel ticket={makeTicket('RESOLVED')} onTransitioned={jest.fn()} />);
    expect(screen.getByRole('button', { name: /transition to closed/i })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /transition to open/i })).toBeNull();
  });

  it('shows no buttons for CANCELLED status', () => {
    render(<StatusTransitionPanel ticket={makeTicket('CANCELLED')} onTransitioned={jest.fn()} />);
    expect(screen.queryByRole('button')).toBeNull();
    expect(screen.getByText(/no further transitions available/i)).toBeInTheDocument();
  });
});

describe('StatusTransitionPanel — error handling', () => {
  afterEach(() => {
    mockTransitionStatus.mockReset();
  });

  it('displays the error message from the API on a failed transition', async () => {
    const apiError: ErrorResponse = {
      status: 422,
      message: 'Transition from OPEN to CLOSED is not allowed',
      errors: [],
    };
    mockTransitionStatus.mockRejectedValue(apiError);

    render(<StatusTransitionPanel ticket={makeTicket('OPEN')} onTransitioned={jest.fn()} />);

    const button = screen.getByRole('button', { name: /transition to in progress/i });
    await userEvent.click(button);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
      expect(screen.getByText('Transition from OPEN to CLOSED is not allowed')).toBeInTheDocument();
    });
  });

  it('calls onTransitioned with the updated ticket on success', async () => {
    const updatedTicket = makeTicket('IN_PROGRESS');
    mockTransitionStatus.mockResolvedValue(updatedTicket);
    const onTransitioned = jest.fn();

    render(<StatusTransitionPanel ticket={makeTicket('OPEN')} onTransitioned={onTransitioned} />);

    const button = screen.getByRole('button', { name: /transition to in progress/i });
    await userEvent.click(button);

    await waitFor(() => {
      expect(onTransitioned).toHaveBeenCalledWith(updatedTicket);
    });
  });
});
