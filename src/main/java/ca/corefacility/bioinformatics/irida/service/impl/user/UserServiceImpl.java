package ca.corefacility.bioinformatics.irida.service.impl.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.user.Group;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectUserJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.user.UserGroupJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.user.UserRepository;
import ca.corefacility.bioinformatics.irida.service.impl.CRUDServiceImpl;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

/**
 * Implementation of the {@link UserService}.
 * 
 */
@Transactional
@Service
public class UserServiceImpl extends CRUDServiceImpl<Long, User> implements UserService {
	/**
	 * The property name to use for passwords on the {@link User} class.
	 */
	private static final String PASSWORD_PROPERTY = "password";
	/**
	 * The property name to use for expired credentials on the {@link User}
	 * class.
	 */
	private static final String CREDENTIALS_NON_EXPIRED_PROPERTY = "credentialsNonExpired";
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	/**
	 * A reference to the user repository used to manage users.
	 */
	private UserRepository userRepository;
	/**
	 * A reference to the project user join repository.
	 */
	private ProjectUserJoinRepository pujRepository;
	/**
	 * A reference to the password encoder used by the system for storing
	 * passwords.
	 */
	private PasswordEncoder passwordEncoder;
	/**
	 * A reference to the user group join repository.
	 */
	private UserGroupJoinRepository userGroupRepository;

	private static final Pattern USER_CONSTRAINT_PATTERN;
	
	/**
	 * A user is permitted to change their own password if they did not
	 * successfully log in, but the reason for the login failure is that their
	 * credentials are expired. This permission checks to see that the user is
	 * authenticated, or that the principle in the security context has an
	 * expired password.
	 */
	private static final String CHANGE_PASSWORD_PERMISSIONS = "isFullyAuthenticated() or "
			+ "(principal instanceof T(ca.corefacility.bioinformatics.irida.model.user.User) and !principal.isCredentialsNonExpired())";

	/**
	 * If a user is an administrator, they are permitted to create a user
	 * account with any role. If a user is a manager, then they are only
	 * permitted to create user accounts with a ROLE_USER role.
	 */
	private static final String CREATE_USER_PERMISSIONS = "hasRole('ROLE_ADMIN') or "
			+ "((#u.getSystemRole() == T(ca.corefacility.bioinformatics.irida.model.user.Role).ROLE_USER) and hasRole('ROLE_MANAGER'))";

	/**
	 * If a user is an administrator, they are permitted to update any user
	 * property. If a manager or user is updating an account, they should not be
	 * permitted to change the role of the user (only administrators can create
	 * users with role other than Role.ROLE_USER).
	 */
	static final String UPDATE_USER_PERMISSIONS = "hasRole('ROLE_ADMIN') or "
			+ "(!#properties.containsKey('systemRole') and hasPermission(#uid, 'canUpdateUser'))";

	static {
		String regex = "^USER_(.*)_CONSTRAINT$";
		USER_CONSTRAINT_PATTERN = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Constructor, requires a handle on a validator and a repository.
	 * 
	 * @param userRepository
	 *            the repository used to store instances of {@link User}.
	 * @param validator
	 *            the validator used to validate instances of {@link User}.
	 * @param pujRepository
	 *            the project user join repository.
	 * @param userGroupJoinRepository
	 *            the user group join repository.
	 * @param passwordEncoder
	 *            the password encoder.
	 */
	@Autowired
	public UserServiceImpl(UserRepository userRepository, ProjectUserJoinRepository pujRepository,
			UserGroupJoinRepository userGroupJoinRepository, PasswordEncoder passwordEncoder, Validator validator) {
		super(userRepository, validator, User.class);
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.pujRepository = pujRepository;
		this.userGroupRepository = userGroupJoinRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_USER')")
	public User read(final Long id) {
		return super.read(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_USER')")
	public Iterable<User> findAll() {
		return super.findAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	public void delete(final Long id) {
		super.delete(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_USER')")
	public Page<User> search(Specification<User> specification, int page, int size, Direction order,
			String... sortProperties) {
		return super.search(specification, page, size, order, sortProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize(CHANGE_PASSWORD_PERMISSIONS)
	public User changePassword(Long userId, String password) {
		Set<ConstraintViolation<User>> violations = validatePassword(password);
		if (violations.isEmpty()) {
			String encodedPassword = passwordEncoder.encode(password);
			return super.update(userId, ImmutableMap.of(PASSWORD_PROPERTY, (Object) encodedPassword,
					CREDENTIALS_NON_EXPIRED_PROPERTY, true));
		}

		throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize(CREATE_USER_PERMISSIONS)
	public User create(@Valid User u) {
		String password = u.getPassword();
		u.setPassword(passwordEncoder.encode(password));
		try {
			return super.create(u);
		} catch (DataIntegrityViolationException e) {
			if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
				RuntimeException translated = translateConstraintViolationException((org.hibernate.exception.ConstraintViolationException) e
						.getCause());
				throw translated;
			} else {
				// I can't figure out what the problem was, just keep
				// throwing it up.
				throw new DataIntegrityViolationException(
						"Unexpected DataIntegrityViolationException, cause accompanies.", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize(UPDATE_USER_PERMISSIONS)
	public User update(Long uid, Map<String, Object> properties) {
		if (properties.containsKey(PASSWORD_PROPERTY)) {
			String password = properties.get(PASSWORD_PROPERTY).toString();
			Set<ConstraintViolation<User>> violations = validatePassword(password);
			if (violations.isEmpty()) {
				properties.put(PASSWORD_PROPERTY, passwordEncoder.encode(password));
			} else {
				throw new ConstraintViolationException(violations);
			}
		}

		return super.update(uid, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("permitAll")
	public User getUserByUsername(String username) throws EntityNotFoundException {
		User u = userRepository.loadUserByUsername(username);
		if (u == null) {
			throw new EntityNotFoundException("User could not be found.");
		}
		return u;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#project, 'canReadProject')")
	public Collection<Join<Project, User>> getUsersForProject(Project project) {
		return pujRepository.getUsersForProject(project);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Transactional(readOnly = true)
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#project, 'canReadProject')")
	public Long countUsersForProject(Project project) {
		return pujRepository.countUsersForProject(project);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("permitAll")
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.trace("Loading user with username: [" + username + "].");
		org.springframework.security.core.userdetails.User userDetails = null;
		User u;
		try {
			u = userRepository.loadUserByUsername(username);

			userDetails = new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(),
					u.getAuthorities());
		} catch (EntityNotFoundException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
		return userDetails;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("permitAll")
	public User loadUserByEmail(String email) throws EntityNotFoundException {
		logger.trace("Loading user with email " + email);
		User loadUserByEmail = userRepository.loadUserByEmail(email);
		if (loadUserByEmail == null) {
			throw new EntityNotFoundException("User could not be found with that email address.");
		}
		return loadUserByEmail;
	}

	/**
	 * Validate the password of a {@link User} *before* encoding the password
	 * and passing to super.
	 * 
	 * @param password
	 *            the password to validate.
	 * @return true if valid, false otherwise.
	 */
	private Set<ConstraintViolation<User>> validatePassword(String password) {
		return validator.validateValue(User.class, PASSWORD_PROPERTY, password);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#project, 'canReadProject')")
	public List<User> getUsersAvailableForProject(Project project) {
		return userRepository.getUsersAvailableForProject(project);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#project, 'canReadProject')")
	public Collection<Join<Project, User>> getUsersForProjectByRole(Project project, ProjectRole projectRole) {
		return pujRepository.getUsersForProjectByRole(project, projectRole);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Collection<Join<User, Group>> getUsersForGroup(Group g) throws EntityNotFoundException {
		return userGroupRepository.getUsersForGroup(g);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Override
	public List<User> getUsersWithEmailSubscriptions() {
		return pujRepository.getUsersWithSubscriptions();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasPermission(#user, 'canUpdateUser')")
	public ProjectUserJoin updateEmailSubscription(User user, Project project, boolean subscribed) {
		ProjectUserJoin projectJoinForUser = pujRepository.getProjectJoinForUser(project, user);
		projectJoinForUser.setEmailSubscription(subscribed);

		return pujRepository.save(projectJoinForUser);
	}

	/**
	 * Translate {@link ConstraintViolationException} errors into an appropriate
	 * {@link EntityExistsException}.
	 * 
	 * @param e
	 *            the exception to translate.
	 * @return the translated exception.
	 */
	private RuntimeException translateConstraintViolationException(
			org.hibernate.exception.ConstraintViolationException e) {
		final EntityExistsException UNABLE_TO_PARSE = new EntityExistsException(
				"Could not create user as a duplicate fields exists; however the duplicate field was not included in "
						+ "the ConstraintViolationException, the original cause is included.", e);
		String constraintName = e.getConstraintName();

		if (StringUtils.isEmpty(constraintName)) {
			return UNABLE_TO_PARSE;
		}
		Matcher matcher = USER_CONSTRAINT_PATTERN.matcher(constraintName);
		if (matcher.groupCount() != 1) {
			throw new IllegalArgumentException("The pattern must have capture groups to parse the constraint name.");
		}

		if (!matcher.find()) {
			return UNABLE_TO_PARSE;
		}
		String fieldName = matcher.group(1).toLowerCase();

		return new EntityExistsException("Could not create user as a duplicate field exists: " + fieldName, fieldName);
	}

}
