# How to Invite Team Members

## Method 1: Through GitHub Website

1. **Go to Repository Settings**
   - Navigate to https://github.com/Meghana-HS/AI_Resume_builder
   - Click "Settings" tab

2. **Add Collaborators**
   - Click "Collaborators" in left sidebar
   - Click "Add people"
   - Enter GitHub usernames or email addresses
   - Choose permission level:
     - **Read** - Can view and clone
     - **Triage** - Can manage issues and pull requests
     - **Write** - Can push to branches
     - **Maintain** - Can manage repository settings
     - **Admin** - Full access

3. **Send Invitations**
   - Add custom message
   - Click "Send invitation"

## Method 2: Create a Team (Recommended)

1. **Create Organization** (if you don't have one)
   - Go to GitHub → "+" → "New organization"
   - Choose "Free" plan
   - Enter organization name

2. **Create Team**
   - In organization settings → "Teams"
   - Click "New team"
   - Set team name (e.g., "resume-builder-devs")
   - Add team description
   - Set team permissions

3. **Add Members to Team**
   - Add team members
   - Assign repository access
   - Set permission levels

## Recommended Team Structure

### Frontend Team
- **Permission**: Write access to `frontend/` folder
- **Members**: Add frontend developers

### Backend Team  
- **Permission**: Write access to `Java_backend/` and `MERN_backend/`
- **Members**: Add backend developers

### Team Leads
- **Permission**: Maintain access
- **Members**: Senior developers/tech leads

## Permission Levels Explained

| Level | What they can do | Recommended for |
|-------|------------------|----------------|
| **Read** | View, clone, create issues | Interns, new team members |
| **Triage** | + Manage issues/PRs | QA team, project managers |
| **Write** | + Push code, create branches | Developers |
| **Maintain** | + Manage settings, protect branches | Tech leads |
| **Admin** | Full control | Repository owner |

## Branch Protection Rules

1. **Go to Settings → Branches**
2. **Add branch protection rule**
3. **Protect `main` branch**:
   - Require pull request reviews
   - Require status checks to pass
   - Include administrators
   - Limit who can push

## Communication Channels

### GitHub Discussions
- Enable for general discussions
- Use for planning and questions

### Issues Templates
- Create templates for bugs, features, questions
- Go to Settings → Issues → Templates

### Projects
- Create project boards for sprint planning
- Track progress with Kanban boards
