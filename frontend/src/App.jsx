import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import Landing from './pages/Landing';
import Search from './pages/Search';
import PropertyDetails from './pages/PropertyDetails';
import Booking from './pages/Booking';
import BookingSuccess from './pages/BookingSuccess';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import MyTrips from './pages/MyTrips';
import Wishlist from './pages/Wishlist';
import HostDashboard from './pages/HostDashboard';
import AdminDashboard from './pages/AdminDashboard';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/search" element={<Search />} />
          <Route path="/properties/:slug" element={<PropertyDetails />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/book/:propertyId" element={
            <ProtectedRoute><Booking /></ProtectedRoute>
          } />
          <Route path="/booking-success" element={
            <ProtectedRoute><BookingSuccess /></ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute><Profile /></ProtectedRoute>
          } />
          <Route path="/trips" element={
            <ProtectedRoute><MyTrips /></ProtectedRoute>
          } />
          <Route path="/bookings/:id" element={
            <ProtectedRoute><MyTrips /></ProtectedRoute>
          } />
          <Route path="/wishlist" element={
            <ProtectedRoute><Wishlist /></ProtectedRoute>
          } />
          <Route path="/host" element={
            <ProtectedRoute><HostDashboard /></ProtectedRoute>
          } />
          <Route path="/host/properties/new" element={
            <ProtectedRoute><HostDashboard /></ProtectedRoute>
          } />
          <Route path="/admin" element={
            <ProtectedRoute><AdminDashboard /></ProtectedRoute>
          } />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
