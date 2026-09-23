import React from 'react';
import { render, screen } from '@testing-library/react';
import ErrorNotification from '../components/ErrorNotification';

describe('ErrorNotification', () => {
  it('renders nothing when error prop is null', () => {
    const { container } = render(<ErrorNotification error={null} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders the error message string when error is provided', () => {
    render(<ErrorNotification error="Something went wrong" />);
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('renders the exact error message text', () => {
    const message = 'Ticket not found';
    render(<ErrorNotification error={message} />);
    expect(screen.getByText(message)).toBeInTheDocument();
  });
});
