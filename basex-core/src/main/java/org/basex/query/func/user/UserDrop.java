package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UserDrop extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final User user = checkSessions(toUser(0, qc), qc);
    final String db = exprs.length > 1 ? toDB(1, qc) : null;
    if(user.name().equals(UserText.ADMIN)) throw BXUS_ADMIN.get(info);
    qc.resources.updates().add(new Drop(user, db, qc, ii), qc);
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }

  /** Update primitive. */
  private static final class Drop extends UserUpdate {
    /**
     * Constructor.
     * @param user user
     * @param db database (optional)
     * @param qc query context
     * @param info input info
     */
    private Drop(final User user, final String db, final QueryContext qc, final InputInfo info) {
      super(UpdateType.USERDROP, user, db, qc, info);
    }

    @Override
    public void apply() {
      boolean global = false;
      for(final String db : databases) global |= db == null;
      if(global) {
        users.drop(user, null);
      } else {
        for(final String db : databases) users.drop(user, db);
      }
    }

    @Override
    public String operation() { return "dropped"; }
  }
}
