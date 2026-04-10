import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { isAuthenticated, clearAuthData, getStoredUserName } from '../services/api';

const Navbar = () => {
  const navigate = useNavigate();
  const loggedIn = isAuthenticated();
  const userName = getStoredUserName();

  const handleLogout = () => {
    clearAuthData();
    navigate('/login');
  };

  return (
    <nav style={styles.nav}>
      <Link to={loggedIn ? '/dashboard' : '/login'} style={{ textDecoration: 'none' }}>
        <h2 style={styles.logo}>Banking System</h2>
      </Link>
      <div style={styles.links}>
        {!loggedIn ? (
          <>
            <Link to="/login" style={styles.link}>Login</Link>
            <Link to="/register" style={styles.link}>Register</Link>
          </>
        ) : (
          <>
            <span style={styles.greeting}>Hi, {userName || 'User'}</span>
            <Link to="/dashboard" style={styles.link}>Dashboard</Link>
            <Link to="/transfer" style={styles.link}>Transfer</Link>
            <button onClick={handleLogout} style={styles.logoutBtn}>Logout</button>
          </>
        )}
      </div>
    </nav>
  );
};

const styles = {
  nav: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '16px 32px',
    backgroundColor: '#1a237e',
    color: 'white',
  },
  logo: {
    fontSize: '20px',
    fontWeight: 'bold',
    color: 'white',
    margin: 0,
  },
  links: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
  },
  link: {
    color: 'white',
    textDecoration: 'none',
    fontWeight: '500',
  },
  greeting: {
    color: '#bbdefb',
    fontWeight: '400',
    marginRight: '8px',
  },
  logoutBtn: {
    background: 'transparent',
    border: '1px solid white',
    color: 'white',
    padding: '6px 14px',
    borderRadius: '6px',
    cursor: 'pointer',
    fontWeight: '500',
    fontSize: '14px',
  },
};

export default Navbar;
