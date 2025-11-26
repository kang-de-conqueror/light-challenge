import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Home from '../pages/index';

global.fetch = jest.fn();

const mockSuccessResponse = {
  success: true,
  amount: 1500,
  department: 'SALES',
  requiresManagerApproval: true,
  selectedApprovers: [
    {
      id: 'cfo',
      name: 'CFO',
      email: 'cfo@company.com',
      department: 'FINANCE',
      isManager: true,
    },
  ],
  notificationsSent: ['Slack notification sent to CFO'],
  rulesApplied: ['High Amount with Manager Approval'],
  message: 'Workflow executed successfully. 1 approver(s) notified.',
};

describe('Home Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the form correctly', () => {
    render(<Home />);

    expect(screen.getByText('Invoice Approval Workflow')).toBeInTheDocument();
    expect(screen.getByLabelText(/Invoice Amount/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Department/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Requires Manager Approval/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Execute Workflow/i })).toBeInTheDocument();
  });

  it('renders all department options', () => {
    render(<Home />);

    const departmentSelect = screen.getByLabelText(/Department/i);
    expect(departmentSelect).toBeInTheDocument();

    const departments = ['SALES', 'MARKETING', 'ENGINEERING', 'FINANCE', 'HR', 'OPERATIONS'];
    departments.forEach((dept) => {
      expect(screen.getByRole('option', { name: dept })).toBeInTheDocument();
    });
  });

  it('allows entering amount value', () => {
    render(<Home />);

    const amountInput = screen.getByLabelText(/Invoice Amount/i) as HTMLInputElement;
    fireEvent.change(amountInput, { target: { value: '1500' } });

    expect(amountInput.value).toBe('1500');
  });

  it('allows selecting department', () => {
    render(<Home />);

    const departmentSelect = screen.getByLabelText(/Department/i) as HTMLSelectElement;
    fireEvent.change(departmentSelect, { target: { value: 'MARKETING' } });

    expect(departmentSelect.value).toBe('MARKETING');
  });

  it('allows toggling manager approval checkbox', () => {
    render(<Home />);

    const checkbox = screen.getByLabelText(/Requires Manager Approval/i) as HTMLInputElement;
    expect(checkbox.checked).toBe(false);

    fireEvent.click(checkbox);
    expect(checkbox.checked).toBe(true);

    fireEvent.click(checkbox);
    expect(checkbox.checked).toBe(false);
  });

  it('submit button is disabled when amount is empty', () => {
    render(<Home />);

    const submitButton = screen.getByRole('button', { name: /Execute Workflow/i });
    expect(submitButton).toBeDisabled();
  });

  it('submit button is enabled when amount is provided', () => {
    render(<Home />);

    const amountInput = screen.getByLabelText(/Invoice Amount/i);
    fireEvent.change(amountInput, { target: { value: '1500' } });

    const submitButton = screen.getByRole('button', { name: /Execute Workflow/i });
    expect(submitButton).not.toBeDisabled();
  });

  it('submits form and displays result on success', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => mockSuccessResponse,
    });

    render(<Home />);

    const amountInput = screen.getByLabelText(/Invoice Amount/i);
    fireEvent.change(amountInput, { target: { value: '1500' } });

    const checkbox = screen.getByLabelText(/Requires Manager Approval/i);
    fireEvent.click(checkbox);

    const submitButton = screen.getByRole('button', { name: /Execute Workflow/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Workflow Result')).toBeInTheDocument();
    });

    expect(screen.getByText('CFO')).toBeInTheDocument();
    expect(screen.getByText('cfo@company.com')).toBeInTheDocument();
    expect(screen.getByText('High Amount with Manager Approval')).toBeInTheDocument();
    expect(screen.getByText('Slack notification sent to CFO')).toBeInTheDocument();
  });

  it('displays error message when API fails', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      json: async () => ({ message: 'Invalid department' }),
    });

    render(<Home />);

    const amountInput = screen.getByLabelText(/Invoice Amount/i);
    fireEvent.change(amountInput, { target: { value: '1500' } });

    const submitButton = screen.getByRole('button', { name: /Execute Workflow/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Error')).toBeInTheDocument();
    });

    expect(screen.getByText('Invalid department')).toBeInTheDocument();
  });

  it('displays error when network request fails', async () => {
    (global.fetch as jest.Mock).mockRejectedValueOnce(new Error('Network error'));

    render(<Home />);

    const amountInput = screen.getByLabelText(/Invoice Amount/i);
    fireEvent.change(amountInput, { target: { value: '1500' } });

    const submitButton = screen.getByRole('button', { name: /Execute Workflow/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/Failed to connect to the server/i)).toBeInTheDocument();
    });
  });
});

