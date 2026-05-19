📌 UMak Lost and Found Management System

The UMak Lost and Found Management System is a Java-based desktop application developed to streamline the process of reporting, tracking, and claiming lost items within the University of Makati. This system replaces traditional manual methods with a centralized, digital solution that improves efficiency, accuracy, and user experience.

🚀 Features
🔐 User Authentication System
Secure login for Admin and Users with password visibility toggle
Role-based access control
Enhanced Profile security with current password verification
📦 Lost & Found Item Management
Add, edit, and archive item records
Support for image uploads for both reported items and claims
Track item status (claimed/unclaimed)
🔄 Auto-Archive System
Automatically moves processed items to archive after 30 days
🔎 Search & Filtering
Easily locate items using search functionality
📋 Claim Processing
Users can file claims with optional image proof
Admin can verify and manage claims with a detailed unified UI
📊 Admin Dashboard
Monitor all records and system activities
Manage users, items, and claims efficiently
Track real-time user online/offline status
🔄 Real-Time Data Updates
Immediate reflection of changes in the system
🛠️ Technologies Used
Java (Swing GUI) – for building the desktop application
MySQL – for database management
JDBC (MySQL Connector) – for database connectivity
🧠 System Overview

The system follows a structured architecture that integrates frontend and backend components:

Frontend: Java Swing-based graphical user interface
Backend: MySQL database for storing system data
Core Logic: Java classes handling system operations and user interactions

Main components include:

LoginFrame.java – Handles authentication
UMAKDashboard.java – Main interface
UMAKSystemMain.java – Core logic
DBConnection.java – Database connection
UserStatus.java – User state and session management
🎯 Purpose

This project aims to:

Improve the efficiency of lost and found processes
Provide a centralized platform for item tracking
Reduce manual errors and time delays
Enhance user convenience and accessibility
⚠️ Limitations
Desktop-based only (no web/mobile version)
No real-time notifications (SMS/Email)
👨‍💻 Developers
Leader: Miel Angelus Majaba
Members:
Kurt Daniell Aleta
Darryl Jay Castillo
Kate Evangelista
Gian Alwin Fernandez
Hannah Mae Intia
Aeron Malabana
Nathan Enzo Saludo
Aljun Earl Saludo
Jelden San Pedro
📌 Future Improvements
Add image upload for items
Implement email/SMS notifications
Develop web/mobile version
Improve security with password encryption
Apply MVC architecture for better structure
