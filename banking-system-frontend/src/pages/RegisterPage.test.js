import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import RegisterPage from './RegisterPage';
import * as api from '../services/api';

// Mock the api module
jest.mock('../services/api', () => ({
  registerUser: jest.fn(),
  saveAuthData: jest.fn(),
}));

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const renderRegisterPage = () => {
  return render(
    <BrowserRouter>
      <RegisterPage />
    </BrowserRouter>
  );
};

describe('RegisterPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders register form with all fields', () => {
    renderRegisterPage();

    expect(screen.getByText('Create Account')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter your name')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter your email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Create a password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
  });

  test('renders link to login page', () => {
    renderRegisterPage();

    expect(screen.getByText('Login here')).toBeInTheDocument();
  });

  test('submits form with correct data - success', async () => {
    api.registerUser.mockResolvedValue({
      data: {
        success: true,
        message: 'Registration successful',
        token: 'mock-token',
        userId: 1,
        name: 'Mary Anger',
        email: 'mary@example.com',
      },
    });

    renderRegisterPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your name'), 'Mary Anger');
    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Create a password'), '123456');
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(api.registerUser).toHaveBeenCalledWith({
        name: 'Mary Anger',
        email: 'mary@example.com',
        password: '123456',
      });
    });

    await waitFor(() => {
      expect(api.saveAuthData).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.getByText('Registration successful! Redirecting...')).toBeInTheDocument();
    });
  });

  test('submits form - registration failure shows error', async () => {
    api.registerUser.mockRejectedValue({
      response: {
        data: { message: 'Email already exists' },
      },
    });

    renderRegisterPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your name'), 'Mary Anger');
    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Create a password'), '123456');
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(screen.getByText('Email already exists')).toBeInTheDocument();
    });

    expect(api.saveAuthData).not.toHaveBeenCalled();
  });

  test('shows generic error when no response message', async () => {
    api.registerUser.mockRejectedValue(new Error('Network error'));

    renderRegisterPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your name'), 'Mary Anger');
    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Create a password'), '123456');
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(screen.getByText('Registration failed. Please try again.')).toBeInTheDocument();
    });
  });

  test('button shows loading state during submission', async () => {
    let resolvePromise;
    api.registerUser.mockImplementation(() => new Promise((resolve) => { resolvePromise = resolve; }));

    renderRegisterPage();

    await userEvent.type(screen.getByPlaceholderText('Enter your name'), 'Mary Anger');
    await userEvent.type(screen.getByPlaceholderText('Enter your email'), 'mary@example.com');
    await userEvent.type(screen.getByPlaceholderText('Create a password'), '123456');
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    expect(screen.getByRole('button', { name: /registering/i })).toBeDisabled();

    resolvePromise({ data: { success: true, token: 't', userId: 1, name: 'M', email: 'e' } });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /register/i })).not.toBeDisabled();
    });
  });
});
