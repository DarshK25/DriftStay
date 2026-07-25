import React from 'react';
import useAuthStore from '../store/authStore';
import { User, Mail, Phone, Shield } from 'lucide-react';

export default function Profile() {
  const { user, logout } = useAuthStore();

  return (
    <div className="pt-24 pb-16">
      <div className="max-w-3xl mx-auto px-4">
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-primary-600 to-primary-800 px-8 py-12 text-white">
            <div className="w-20 h-20 rounded-full bg-white/20 flex items-center justify-center mb-4">
              <span className="text-3xl font-bold">{user?.firstName?.[0]}{user?.lastName?.[0]}</span>
            </div>
            <h1 className="text-2xl font-display font-bold">{user?.firstName} {user?.lastName}</h1>
            <p className="text-white/80 text-sm">{user?.email}</p>
          </div>

          {/* Details */}
          <div className="p-8 space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="flex items-start gap-3">
                <User className="w-5 h-5 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-sm text-gray-500">Full Name</p>
                  <p className="font-medium">{user?.firstName} {user?.lastName}</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Mail className="w-5 h-5 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-sm text-gray-500">Email</p>
                  <p className="font-medium">{user?.email}</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Phone className="w-5 h-5 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-sm text-gray-500">Phone</p>
                  <p className="font-medium">{user?.phone || 'Not provided'}</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Shield className="w-5 h-5 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-sm text-gray-500">Member Since</p>
                  <p className="font-medium">2026</p>
                </div>
              </div>
            </div>

            <div className="border-t border-gray-100 pt-6">
              <button onClick={logout}
                className="px-6 py-2.5 border border-red-300 text-red-600 rounded-full text-sm font-medium hover:bg-red-50 transition-colors">
                Sign out
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
