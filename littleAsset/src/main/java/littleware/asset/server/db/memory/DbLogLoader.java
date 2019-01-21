package littleware.asset.server.db.memory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import littleware.asset.IdWithClock;
import littleware.db.DbReader;

@SuppressWarnings("unchecked")
class DbLogLoader implements DbReader<List<IdWithClock>, Long> {


    @Override
    public List<IdWithClock> loadObject(Long minTransactionIn) {
        throw new UnsupportedOperationException();
    }
}
