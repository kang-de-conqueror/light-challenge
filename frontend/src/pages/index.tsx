import { useState } from 'react';

interface Approver {
  id: string;
  name: string;
  email: string;
  department: string | null;
  isManager: boolean;
}

interface WorkflowResponse {
  success: boolean;
  amount: number;
  department: string;
  requiresManagerApproval: boolean;
  selectedApprovers: Approver[];
  notificationsSent: string[];
  rulesApplied: string[];
  message: string;
  error?: string;
}

const DEPARTMENTS = ['SALES', 'MARKETING', 'ENGINEERING', 'FINANCE', 'HR', 'OPERATIONS'];

export default function Home() {
  const [amount, setAmount] = useState<string>('');
  const [department, setDepartment] = useState<string>('SALES');
  const [requiresManagerApproval, setRequiresManagerApproval] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<WorkflowResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await fetch('/api/workflow/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          amount: parseFloat(amount),
          department,
          requiresManagerApproval,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || 'An error occurred');
      } else {
        setResult(data);
      }
    } catch (err) {
      setError('Failed to connect to the server. Make sure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Invoice Approval Workflow</h1>
          <p className="mt-2 text-gray-600">Submit an invoice for approval routing</p>
        </div>

        <div className="bg-white shadow rounded-lg p-6 mb-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="amount" className="block text-sm font-medium text-gray-700">
                Invoice Amount (USD)
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span className="text-gray-500 sm:text-sm">$</span>
                </div>
                <input
                  type="number"
                  id="amount"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="block w-full pl-7 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900"
                  placeholder="0.00"
                  step="0.01"
                  min="0"
                  required
                />
              </div>
            </div>

            <div>
              <label htmlFor="department" className="block text-sm font-medium text-gray-700">
                Department
              </label>
              <select
                id="department"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                className="mt-1 block w-full pl-3 pr-10 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900"
              >
                {DEPARTMENTS.map((dept) => (
                  <option key={dept} value={dept}>
                    {dept}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex items-center">
              <input
                type="checkbox"
                id="managerApproval"
                checked={requiresManagerApproval}
                onChange={(e) => setRequiresManagerApproval(e.target.checked)}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="managerApproval" className="ml-2 block text-sm text-gray-700">
                Requires Manager Approval
              </label>
            </div>

            <button
              type="submit"
              disabled={loading || !amount}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {loading ? 'Processing...' : 'Execute Workflow'}
            </button>
          </form>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <p className="mt-1 text-sm text-red-700">{error}</p>
              </div>
            </div>
          </div>
        )}

        {result && (
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Workflow Result</h2>

            <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-4">
              <p className="text-green-800 font-medium">{result.message}</p>
            </div>

            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="bg-gray-50 p-3 rounded">
                <p className="text-sm text-gray-500">Amount</p>
                <p className="font-semibold text-gray-900">${result.amount.toFixed(2)}</p>
              </div>
              <div className="bg-gray-50 p-3 rounded">
                <p className="text-sm text-gray-500">Department</p>
                <p className="font-semibold text-gray-900">{result.department}</p>
              </div>
              <div className="bg-gray-50 p-3 rounded col-span-2">
                <p className="text-sm text-gray-500">Manager Approval Required</p>
                <p className="font-semibold text-gray-900">
                  {result.requiresManagerApproval ? 'Yes' : 'No'}
                </p>
              </div>
            </div>

            <div className="mb-6">
              <h3 className="text-lg font-medium text-gray-900 mb-2">Rules Applied</h3>
              <ul className="list-disc list-inside text-gray-700">
                {result.rulesApplied.map((rule, index) => (
                  <li key={index}>{rule}</li>
                ))}
              </ul>
            </div>

            <div className="mb-6">
              <h3 className="text-lg font-medium text-gray-900 mb-2">Selected Approvers</h3>
              {result.selectedApprovers.length > 0 ? (
                <div className="space-y-3">
                  {result.selectedApprovers.map((approver) => (
                    <div key={approver.id} className="bg-blue-50 p-3 rounded-lg">
                      <p className="font-semibold text-gray-900">{approver.name}</p>
                      <p className="text-sm text-gray-600">{approver.email}</p>
                      <div className="flex gap-2 mt-1">
                        {approver.department && (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-200 text-gray-800">
                            {approver.department}
                          </span>
                        )}
                        {approver.isManager && (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-purple-200 text-purple-800">
                            Manager
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">No approvers selected</p>
              )}
            </div>

            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">Notifications Sent</h3>
              <ul className="list-disc list-inside text-gray-700">
                {result.notificationsSent.map((notification, index) => (
                  <li key={index}>{notification}</li>
                ))}
              </ul>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
