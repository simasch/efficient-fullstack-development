package ch.martinelli.tm.domain;

/**
 * The lifecycle of a task. The allowed transitions are the first business rule of the
 * application: a task starts OPEN, is worked on IN_PROGRESS, may get BLOCKED, and ends
 * DONE. A DONE task can be reopened.
 */
public enum TaskStatus {

	OPEN, IN_PROGRESS, BLOCKED, DONE;

	public boolean canTransitionTo(TaskStatus target) {
		if (target == this) {
			return true;
		}
		return switch (this) {
			case OPEN -> target == IN_PROGRESS || target == DONE;
			case IN_PROGRESS -> target == BLOCKED || target == DONE || target == OPEN;
			case BLOCKED -> target == IN_PROGRESS || target == OPEN;
			case DONE -> target == OPEN;
		};
	}

}
