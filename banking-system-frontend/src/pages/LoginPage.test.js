import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from './LoginPage';
import * as api from '../services/api';

// Mock the api module
jest.mock('../services/api', () => ({
  loginUser: jest.fn(),
  saveAuthData: jest.fn(),
}));

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const renderLoginPage = () => {
  return render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>
  );
};

describe('LoginPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders login form with email and password fields', () => {
    renderLoginPage();

    expect(screen.getByText('Welcome Back')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter your email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter your password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  test('renders link to register page', () => {
    renderLoginPage();

    expect(screen.getByText('Register here')).toBeInTheDocument();
  });

  test('submits form with correct data - success', async () => {
    api.loginUser.mockResolvedValue({
      data: {
        success: true,
        message: 'Login successful',
        token: 'mock-token',
        userId: 1,
        name: 'Mary Anger',
        email: 'mary@example.com',
      },
    });

    renderLoginPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), '123456');
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(api.loginUser).toHaveBeenCalledWith({
        email: 'mary@example.com',
        password: '123456',
      });
    });

    await waitFor(() => {
      expect(api.saveAuthData).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.getByText('Login successful! Redirecting...')).toBeInTheDocument();
    });
  });

  test('submits form - login failure shows error', async () => {
    api.loginUser.mockRejectedValue({
      response: {
        data: { message: 'Invalid password' },
      },
    });

    renderLoginPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), 'wrong');
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByText('Invalid password')).toBeInTheDocument();
    });

    expect(api.saveAuthData).not.toHaveBeenCalled();
  });

  test('shows generic error when no response message', async () => {
    api.loginUser.mockRejectedValue(new Error('Network error'));

    renderLoginPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), '123456');
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByText('Login failed. Please try again.')).toBeInTheDocument();
    });
  });

  test('button shows loading state during submission', async () => {
    let resolvePromise;
    api.loginUser.mockImplementation(() => new Promise((resolve) => { resolvePromise = resolve; }));

    renderLoginPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Enter your password'), '123456');
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    expect(screen.getByRole('button', { name: /logging in/i })).toBeDisabled();

    resolvePromise({ data: { success: true, token: 't', userId: 1, name: 'M', email: 'e' } });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /login/i })).not.toBeDisabled();
    });
  });
});
