import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAccountsByUser, getStoredUserId, getStoredUserName, clearAuthData } from '../services/api';

const Dashboard = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const userId = getStoredUserId();
  const userName = getStoredUserName();

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const response = await getAccountsByUser(userId);
        setAccounts(response.data);
      } catch (err) {
        setError('Failed to load account data.');
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchAccounts();
    } else {
      setLoading(false);
      setError('User not found. Please log in again.');
    }
  }, [userId]);

  const handleLogout = () => {
    clearAuthData();
    navigate('/login');
  };

  return (
    <div className="page-container">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <button className="btn-secondary" onClick={handleLogout}>Logout</button>
      </div>

      <p className="subtitle">Welcome, {userName || 'User'}!</p>

      {loading && <p className="loading-text">Loading accounts...</p>}

      {error && <div className="message error">{error}</div>}

      {!loading && accounts.length > 0 && (
        <div className="accounts-list">
          <h2>Your Accounts</h2>
          {accounts.map((account) => (
            <div key={account.accountId} className="account-card">
              <div className="account-info">
                <span className="account-label">Account ID</span>
                <span className="account-value">#{account.accountId}</span>
              </div>
              <div className="account-info">
                <span className="account-label">Balance</span>
                <span className="account-balance">${parseFloat(account.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && accounts.length === 0 && !error && (
        <p>No accounts found.</p>
      )}

      <div className="nav-links">
        <Link to="/transfer">Make a Transfer</Link>
      </div>
    </div>
  );
};

export default Dashboard;
