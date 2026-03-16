# Team Collaboration Workflow

## Repository Structure
```
AI_Resume_builder/
├── frontend/          # React frontend
├── Java_backend/      # Spring Boot backend  
├── MERN_backend/      # Node.js backend (alternative)
└── TEAM_WORKFLOW.md   # This file
```

## Branching Strategy

### Main Branches
- `main` - Production-ready code
- `develop` - Integration branch for features

### Feature Branches
- `feature/username-description` - New features
- `bugfix/username-description` - Bug fixes
- `hotfix/username-description` - Critical fixes

## Workflow Steps

### 1. Starting Work
```bash
# Always start from latest develop
git checkout develop
git pull origin develop

# Create your feature branch
git checkout -b feature/yourname-resume-builder-fix
```

### 2. Making Changes
- Work on your specific area:
  - Frontend developers: `frontend/` folder
  - Backend developers: `Java_backend/` or `MERN_backend/` folder
- Commit frequently with clear messages

### 3. Commit Standards
```bash
# Good commit messages
git commit -m "feat: add user authentication"
git commit -m "fix: resolve dashboard API error"
git commit -m "docs: update API documentation"

# Bad commit messages
git commit -m "fixed stuff"
git commit -m "update"
```

### 4. Pushing Changes
```bash
# Push your feature branch
git push origin feature/yourname-resume-builder-fix
```

### 5. Creating Pull Requests
1. Go to GitHub repository
2. Click "New Pull Request"
3. Select your branch → `develop`
4. Fill PR template
5. Request review from team members

### 6. Code Review Process
- At least 1 team member must review
- All automated tests must pass
- Resolve review comments
- Merge to `develop` after approval

### 7. Deployment
- `develop` → `main` for production releases
- Only team leads should merge to main

## Team Roles

### Frontend Team
- React components
- UI/UX improvements
- API integration
- Testing frontend

### Backend Team  
- Java Spring Boot development
- API endpoints
- Database management
- Authentication/authorization

### Full Stack Developers
- Work across both frontend/backend
- Integration tasks
- End-to-end testing

## Communication

### Daily Standups
- What did you complete yesterday?
- What will you work on today?
- Any blockers?

### Issues & Project Management
- Use GitHub Issues for bugs/features
- Use Projects for sprint planning
- Label issues: `bug`, `enhancement`, `documentation`

## Conflict Resolution

### Merge Conflicts
```bash
# When conflicts occur
git fetch origin
git merge origin/develop
# Resolve conflicts in your IDE
git add .
git commit -m "resolve merge conflicts"
git push origin feature/yourname-branch
```

### Code Standards
- Follow existing code style
- Add comments for complex logic
- Write tests for new features
- Update documentation

## Environment Setup

### Development Environment
```bash
# Frontend
cd frontend
npm install
npm start

# Backend (Java)
cd Java_backend
./mvnw spring-boot:run

# Backend (MERN - if needed)
cd MERN_backend
npm install
npm run dev
```

### Database
- Share database connection details securely
- Use environment variables
- Never commit credentials

## Testing

### Before Creating PR
- Frontend: `npm test`
- Backend: `./mvnw test`
- Manual testing of your feature
- Check for console errors

## Deployment

### Staging
- Deploy `develop` branch to staging server
- Team testing before production

### Production
- Only deploy `main` branch
- Create release tags
- Update changelog
