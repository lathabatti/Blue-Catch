/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  ShieldCheck, 
  Lock, 
  Mail, 
  User, 
  Eye, 
  EyeOff, 
  UserPlus, 
  LogIn, 
  Anchor, 
  Compass, 
  Waves,
  Ship,
  Check,
  AlertTriangle,
  FileKey,
  Shield,
  HelpCircle
} from 'lucide-react';
import { AdminUser } from '../types';

interface AuthPageProps {
  onLoginSuccess: (user: AdminUser) => void;
  defaultEmail?: string;
}

export default function AuthPage({ onLoginSuccess, defaultEmail = 'sanjusmily128@gmail.com' }: AuthPageProps) {
  const [isLogin, setIsLogin] = useState(() => {
    const saved = localStorage.getItem('fish_admins');
    if (!saved) return false;
    try {
      const list = JSON.parse(saved);
      return list.length > 0;
    } catch {
      return false;
    }
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Form Fields
  const [email, setEmail] = useState(defaultEmail);
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [role, setRole] = useState<'super_admin' | 'moderator' | 'operator'>('super_admin');
  const [agreeToTerms, setAgreeToTerms] = useState(true);

  // Password Recovery Mode
  const [isRecovery, setIsRecovery] = useState(false);
  const [recoveryEmail, setRecoveryEmail] = useState('');

  // Local storage credentials list setup (to make signup persistent!)
  const getRegisteredAdmins = (): Array<{ email: string; pass: string; user: AdminUser }> => {
    const saved = localStorage.getItem('fish_admins');
    const defaults: Array<{ email: string; pass: string; user: AdminUser }> = [];
    if (!saved) {
      localStorage.setItem('fish_admins', JSON.stringify(defaults));
      return defaults;
    }
    return JSON.parse(saved);
  };

  const handleToggleMode = () => {
    setIsLogin(!isLogin);
    setIsRecovery(false);
    setErrorMessage('');
    setSuccessMessage('');
    setPassword('');
  };

  const handleLoginSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!email || !password) {
      setErrorMessage('Please fill in all security fields.');
      return;
    }

    setLoading(true);

    // Simulate network authentication latency
    setTimeout(() => {
      const admins = getRegisteredAdmins();
      const matched = admins.find(
        a => a.email.toLowerCase() === email.toLowerCase().trim() && a.pass === password
      );

      if (matched) {
        setSuccessMessage(`Authorized! Welcome aboard, ${matched.user.name}.`);
        setTimeout(() => {
          onLoginSuccess(matched.user);
          setLoading(false);
        }, 800);
      } else {
        setErrorMessage('Access Denied. Invalid master email or password credential.');
        setLoading(false);
      }
    }, 1000);
  };

  const handleSignupSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!fullName || !email || !password) {
      setErrorMessage('All registration fields are required.');
      return;
    }

    if (password.length < 6) {
      setErrorMessage('Passwords must be at least 6 characters.');
      return;
    }

    if (!agreeToTerms) {
      setErrorMessage('You must agree to the terms.');
      return;
    }

    setLoading(true);

    setTimeout(() => {
      const admins = getRegisteredAdmins();
      const exists = admins.some(a => a.email.toLowerCase() === email.toLowerCase().trim());

      if (exists) {
        setErrorMessage('This administrative email is already registered in our databases.');
        setLoading(false);
        return;
      }

      const newAdmin: AdminUser = {
        id: `ADM-${Math.floor(Math.random() * 900 + 100)}`,
        name: fullName,
        email: email.toLowerCase().trim(),
        role,
        createdAt: new Date().toISOString().split('T')[0]
      };

      const updatedList = [...admins, { email: email.toLowerCase().trim(), pass: password, user: newAdmin }];
      localStorage.setItem('fish_admins', JSON.stringify(updatedList));

      setSuccessMessage('Registration cataloged! Security keys created.');
      
      setTimeout(() => {
        // Log them in immediately
        onLoginSuccess(newAdmin);
        setLoading(false);
      }, 1000);
    }, 1200);
  };

  const handleRecoverySubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!recoveryEmail) {
      setErrorMessage('Please specify your registered administrative email.');
      return;
    }

    setLoading(true);

    setTimeout(() => {
      const admins = getRegisteredAdmins();
      const matched = admins.find(a => a.email.toLowerCase() === recoveryEmail.toLowerCase().trim());

      if (matched) {
        setSuccessMessage(`A secure credential reset token has been dispatched to ${recoveryEmail}.`);
      } else {
        setErrorMessage('Email address not found in the administrative registry.');
      }
      setLoading(false);
    }, 1000);
  };

  return (
    <div className="min-h-screen w-full bg-slate-950 flex flex-col md:flex-row text-slate-300 font-sans selection:bg-cyan-500/20 selection:text-cyan-300 overflow-x-hidden">
      
      {/* LEFT PANEL: Clean Maritime Branding */}
      <div className="flex-1 min-h-[320px] md:min-h-screen relative overflow-hidden bg-slate-900 border-b md:border-b-0 md:border-r border-slate-800 flex flex-col justify-between p-8 md:p-12">
        {/* Animated Background Gradients & Ocean Ripple effect */}
        <div className="absolute inset-0 bg-gradient-to-tr from-slate-950 via-slate-900 to-cyan-950/10 z-0" />
        <div className="absolute top-1/4 -left-1/4 w-96 h-96 bg-cyan-500/5 rounded-full blur-3xl" />
        <div className="absolute bottom-1/4 -right-1/4 w-96 h-96 bg-indigo-500/5 rounded-full blur-3xl" />
        
        {/* Top Header */}
        <div className="relative z-10 flex items-center gap-2">
          <div className="w-10 h-10 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400 shadow-lg shadow-cyan-500/5">
            <Anchor size={20} />
          </div>
          <div>
            <span className="text-sm font-semibold tracking-wider text-slate-200 font-mono block">BLUE CATCH</span>
            <span className="text-[10px] text-slate-500 font-mono tracking-widest uppercase">Admin Portal</span>
          </div>
        </div>

        {/* Dynamic Maritime Branding */}
        <div className="relative z-10 my-8 md:my-0 space-y-4 max-w-lg">
          <h1 className="text-3xl md:text-4xl font-extrabold text-white tracking-tight leading-tight">
            Managing Freshness, <br />
            <span className="bg-gradient-to-r from-cyan-400 via-teal-300 to-emerald-400 bg-clip-text text-transparent">
              One Catch at a Time
            </span>
          </h1>
          <p className="text-sm text-slate-400 leading-relaxed">
            Authorized administrative platform for tracking fresh catch orders, onboarding local vendors, regulating logistics, and analyzing supply chain delivery metrics.
          </p>
        </div>

        {/* Footer info */}
        <div className="relative z-10 text-xs text-slate-500 font-mono flex items-center justify-between">
          <span>Blue Catch Management</span>
          <span className="flex items-center gap-1 text-slate-500">
            Secure Connection
          </span>
        </div>
      </div>

      {/* RIGHT PANEL: Auth Wizard Form Container */}
      <div className="flex-1 flex items-center justify-center p-6 md:p-12 bg-slate-950/95 relative">
        <div className="w-full max-w-md space-y-8 relative z-10">
          
          <AnimatePresence mode="wait">
            {isRecovery ? (
              /* PASSWORD RECOVERY VIEW */
              <motion.div
                key="recovery"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.2 }}
                className="space-y-6"
              >
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">Reset Password</h2>
                  <p className="text-xs text-slate-500 mt-1.5">
                    Enter your registered email address to receive password recovery instructions.
                  </p>
                </div>

                <form onSubmit={handleRecoverySubmit} className="space-y-4">
                  <div>
                    <label className="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1.5">
                      Email Address
                    </label>
                    <div className="relative">
                      <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
                      <input
                        type="email"
                        required
                        value={recoveryEmail}
                        onChange={(e) => setRecoveryEmail(e.target.value)}
                        placeholder="admin@bluecatch.com"
                        className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-800 hover:border-slate-700/80 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/10 rounded-xl text-xs text-slate-200 transition-all placeholder:text-slate-600"
                      />
                    </div>
                  </div>

                  {errorMessage && (
                    <div className="p-3 bg-red-500/10 border border-red-500/20 text-red-400 text-xs rounded-xl flex items-start gap-2">
                      <AlertTriangle size={15} className="shrink-0 mt-0.5" />
                      <span>{errorMessage}</span>
                    </div>
                  )}

                  {successMessage && (
                    <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs rounded-xl flex items-start gap-2">
                      <Check size={15} className="shrink-0 mt-0.5" />
                      <span>{successMessage}</span>
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-2.5 bg-cyan-600 hover:bg-cyan-500 disabled:bg-slate-800 text-slate-950 font-bold text-xs rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-cyan-600/10 hover:shadow-cyan-500/20 transition-all"
                  >
                    {loading ? (
                      <span className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <>
                        <FileKey size={14} /> Send Recovery Instructions
                      </>
                    )}
                  </button>
                </form>

                <div className="text-center">
                  <button
                    onClick={() => {
                      setIsRecovery(false);
                      setErrorMessage('');
                      setSuccessMessage('');
                    }}
                    className="text-xs text-cyan-400 hover:text-cyan-300 underline underline-offset-4 transition-colors"
                  >
                    Back to Sign In
                  </button>
                </div>
              </motion.div>
            ) : isLogin ? (
              /* LOGIN FORM VIEW */
              <motion.div
                key="login"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.2 }}
                className="space-y-6"
              >
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">Sign In</h2>
                  <p className="text-xs text-slate-400 mt-1.5">
                    Welcome to the Blue Catch Admin Portal.
                  </p>
                </div>

                <form onSubmit={handleLoginSubmit} className="space-y-4">
                  {/* Email Input */}
                  <div>
                    <label className="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1.5">
                      Email Address
                    </label>
                    <div className="relative">
                      <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
                      <input
                        type="email"
                        required
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="admin@bluecatch.com"
                        className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-800 hover:border-slate-700/80 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/10 rounded-xl text-xs text-slate-200 transition-all placeholder:text-slate-600"
                      />
                    </div>
                  </div>

                  {/* Password Input */}
                  <div>
                    <div className="flex items-center justify-between mb-1.5">
                      <label className="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider block">
                        Password
                      </label>
                      <button
                        type="button"
                        onClick={() => setIsRecovery(true)}
                        className="text-[10px] text-cyan-400 hover:text-cyan-300 font-mono hover:underline"
                      >
                        Forgot Password?
                      </button>
                    </div>
                    <div className="relative">
                      <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
                      <input
                        type={showPassword ? 'text' : 'password'}
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="w-full pl-10 pr-10 py-2.5 bg-slate-900 border border-slate-800 hover:border-slate-700/80 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/10 rounded-xl text-xs text-slate-200 transition-all placeholder:text-slate-600"
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
                      >
                        {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
                      </button>
                    </div>
                  </div>

                  {errorMessage && (
                    <div className="p-3 bg-red-500/10 border border-red-500/20 text-red-400 text-xs rounded-xl flex items-start gap-2">
                      <AlertTriangle size={15} className="shrink-0 mt-0.5" />
                      <span>{errorMessage}</span>
                    </div>
                  )}

                  {successMessage && (
                    <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs rounded-xl flex items-start gap-2">
                      <Check size={15} className="shrink-0 mt-0.5" />
                      <span>{successMessage}</span>
                    </div>
                  )}

                  {/* Submit Button */}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-2.5 bg-cyan-500 hover:bg-cyan-400 disabled:bg-slate-800 text-slate-950 font-bold text-xs rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/10 hover:shadow-cyan-400/20 transition-all"
                  >
                    {loading ? (
                      <span className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <>
                        <LogIn size={14} /> Sign In
                      </>
                    )}
                  </button>
                </form>

                {/* Footer Toggle */}
                <div className="pt-4 border-t border-slate-900 text-center text-xs text-slate-500">
                  Need an admin account?{' '}
                  <button
                    onClick={handleToggleMode}
                    className="text-cyan-400 hover:text-cyan-300 font-medium underline underline-offset-4"
                  >
                    Register here
                  </button>
                </div>
              </motion.div>
            ) : (
              /* SIGNUP FORM VIEW */
              <motion.div
                key="signup"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.2 }}
                className="space-y-5"
              >
                <div>
                  <h2 className="text-2xl font-bold text-white tracking-tight">Create Admin Account</h2>
                  <p className="text-xs text-slate-400 mt-1.5">
                    Register a new administrator profile for system management.
                  </p>
                </div>

                <form onSubmit={handleSignupSubmit} className="space-y-3.5">
                  {/* Full Name */}
                  <div>
                    <label className="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1">
                      Full Name
                    </label>
                    <div className="relative">
                      <User className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={15} />
                      <input
                        type="text"
                        required
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        placeholder="Officer Diana Prince"
                        className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700/80 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/10 rounded-xl text-xs text-slate-200 transition-all placeholder:text-slate-600"
                      />
                    </div>
                  </div>

                  {/* Email */}
                  <div>
                    <label className="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1">
                      Email Address
                    </label>
                    <div className="relative">
                      <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={15} />
                      <input
                        type="email"
                        required
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="admin@bluecatch.com"
                        className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700/80 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/10 rounded-xl text-xs text-slate-200 transition-all placeholder:text-slate-600"
                      />
                    </div>
                  </div>

                  {/* Role Selector */}
                  <div>
                    <label className="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1.5">
                      Administrative Role
                    </label>
                    <div className="grid grid-cols-3 gap-2">
                      <button
                        type="button"
                        onClick={() => setRole('super_admin')}
                        className={`py-1.5 rounded-lg border text-[10px] font-mono transition-all ${
                          role === 'super_admin'
                            ? 'bg-cyan-500/10 border-cyan-500 text-cyan-400 font-bold'
                            : 'bg-slate-900 border-slate-800 text-slate-500 hover:border-slate-700'
                        }`}
                      >
                        Super Admin
                      </button>
                      <button
                        type="button"
                        onClick={() => setRole('moderator')}
                        className={`py-1.5 rounded-lg border text-[10px] font-mono transition-all ${
                          role === 'moderator'
                            ? 'bg-teal-500/10 border-teal-500 text-teal-400 font-bold'
                            : 'bg-slate-900 border-slate-800 text-slate-500 hover:border-slate-700'
                        }`}
                      >
                        Moderator
                      </button>
                      <button
                        type="button"
                        onClick={() => setRole('operator')}
                        className={`py-1.5 rounded-lg border text-[10px] font-mono transition-all ${
                          role === 'operator'
                            ? 'bg-emerald-500/10 border-emerald-500 text-emerald-400 font-bold'
                            : 'bg-slate-900 border-slate-800 text-slate-500 hover:border-slate-700'
                        }`}
                      >
                        Operator
                      </button>
                    </div>
                    <span className="text-[9px] text-slate-500 font-mono mt-1 block">
                      {role === 'super_admin' && 'Full privileges. Financial controls and system settings override.'}
                      {role === 'moderator' && 'Manages vendor approvals, complaints, and delivery onboarding.'}
                      {role === 'operator' && 'Read-only access. Active order simulation and data monitoring.'}
                    </span>
                  </div>

                  {/* Password */}
                  <div>
                    <label className="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1">
                      Password
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={14} />
                      <input
                        type={showPassword ? 'text' : 'password'}
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Choose a strong password (Min 6 characters)"
                        className="w-full pl-9 pr-8 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700/80 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/10 rounded-xl text-xs text-slate-200 transition-all placeholder:text-slate-600"
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
                      >
                        {showPassword ? <EyeOff size={14} /> : <Eye size={14} />}
                      </button>
                    </div>
                  </div>

                  {/* Agree to terms check */}
                  <label className="flex items-start gap-2 pt-1 select-none cursor-pointer">
                    <input
                      type="checkbox"
                      checked={agreeToTerms}
                      onChange={(e) => setAgreeToTerms(e.target.checked)}
                      className="mt-0.5 rounded border-slate-800 text-cyan-600 focus:ring-cyan-500/20 bg-slate-900 accent-cyan-500"
                    />
                    <span className="text-[10px] text-slate-400 font-mono leading-tight">
                      I agree to the terms of service, administrator guidelines, and security policies.
                    </span>
                  </label>

                  {errorMessage && (
                    <div className="p-2.5 bg-red-500/10 border border-red-500/20 text-red-400 text-xs rounded-xl flex items-start gap-2">
                      <AlertTriangle size={14} className="shrink-0 mt-0.5" />
                      <span>{errorMessage}</span>
                    </div>
                  )}

                  {successMessage && (
                    <div className="p-2.5 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs rounded-xl flex items-start gap-2">
                      <Check size={14} className="shrink-0 mt-0.5" />
                      <span>{successMessage}</span>
                    </div>
                  )}

                  {/* Signup button */}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-2 bg-cyan-500 hover:bg-cyan-400 disabled:bg-slate-800 text-slate-950 font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 shadow-lg shadow-cyan-500/10 hover:shadow-cyan-400/20 transition-all"
                  >
                    {loading ? (
                      <span className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <>
                        <UserPlus size={14} /> Register Account
                      </>
                    )}
                  </button>
                </form>

                {/* Switch back to login */}
                <div className="pt-3 border-t border-slate-900 text-center text-xs text-slate-500">
                  Already have an account?{' '}
                  <button
                    onClick={handleToggleMode}
                    className="text-cyan-400 hover:text-cyan-300 font-medium underline underline-offset-4"
                  >
                    Sign in here
                  </button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

        </div>
      </div>

    </div>
  );
}
