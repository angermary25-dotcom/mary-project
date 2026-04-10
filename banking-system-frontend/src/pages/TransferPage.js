import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { transfer } from '../services/api';

const TransferPage = () => {
  const [senderAccountId, setSenderAccountId] = useState('');
  const [receiverAccountId, setReceiverAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setResult(null);
    setLoading(true);

    try {
      const response = await transfer({
        senderAccountId: parseInt(senderAccountId),
        receiverAccountId: parseInt(receiverAccountId),
        amount: parseFloat(amount),
      });

      if (response.data.success) {
        setMessage(response.data.message);
        setIsError(false);
        setResult(response.data);
        setSenderAccountId('');
        setReceiverAccountId('');
        setAmount('');
      } else {
        setMessage(response.data.message);
        setIsError(true);
      }
    } catch (error) {
      const msg = error.response?.data?.message || 'Transfer failed. Please try again.';
      setMessage(msg);
      setIsError(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>Transfer Funds</h1>
      <p className="subtitle">Send money to another account</p>

      {message && (
        <div className={`message ${isError ? 'error' : 'success'}`}>
          {message}
        </div>
      )}

      {result && (
        <div className="transfer-result">
          <p><strong>Transaction ID:</strong> #{result.transactionId}</p>
          <p><strong>Sender New Balance:</strong> ${parseFloat(result.senderNewBalance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
          <p><strong>Receiver New Balance:</strong> ${parseFloat(result.receiverNewBalance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <label>Sender Account ID</label>
        <input
          type="number"
          placeholder="Enter sender account ID"
          value={senderAccountId}
          onChange={(e) => setSenderAccountId(e.target.value)}
          required
        />

        <label>Receiver Account ID</label>
        <input
          type="number"
          placeholder="Enter receiver account ID"
          value={receiverAccountId}
          onChange={(e) => setReceiverAccountId(e.target.value)}
          required
        />

        <label>Amount</label>
        <input
          type="number"
          step="0.01"
          min="0.01"
          placeholder="Enter amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Processing...' : 'Transfer'}
        </button>
      </form>

      <div className="nav-links">
        <Link to="/dashboard">Back to Dashboard</Link>
      </div>
    </div>
  );
};

export default TransferPage;
