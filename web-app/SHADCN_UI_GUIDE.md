# FieldSync shadcn/ui Integration Guide

## 1. Purpose

This document defines the recommended approach for integrating and using
shadcn/ui in FieldSync and future React-based applications.

The goal is to use shadcn/ui as a reusable UI foundation while keeping
application-specific branding, business logic, validation, navigation,
authentication and API integrations separate from presentation concerns.

The FieldSync evaluation was performed using:

- React
- Vite
- Tailwind CSS v4
- JavaScript / JSX
- shadcn/ui
- Base UI
- Nova style
- Lucide icons
- Geist typography

The evaluation covered representative interfaces including:

- Dashboard
- User Management
- Customers CRUD

---

## 2. Integration Principle

shadcn/ui should be treated as a UI component foundation rather than as a
complete application framework.

Application logic should remain inside pages, hooks and services.

shadcn components should primarily handle:

- Visual structure
- Inputs and controls
- Cards
- Tables
- Dialogs
- Alerts
- Badges
- Loading states
- Accessible interaction patterns

Do not move API calls, authentication logic or business rules into generated
shadcn component files.

---

## 3. Initial Setup

For an existing Vite React project, initialize shadcn/ui from the frontend
project directory:

```bash
npx shadcn@latest init