/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.jdbc.management;

import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.jdbc.orm.JdbcObjectMapper;
import io.gravitee.repository.management.api.AlertTriggerRepository;
import io.gravitee.repository.management.model.AlertTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcAlertRepository extends JdbcAbstractCrudRepository<AlertTrigger, String> implements AlertTriggerRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(JdbcAlertRepository.class);

    private static final JdbcObjectMapper ORM = JdbcObjectMapper.builder(AlertTrigger.class, "alert_triggers", "id")
            .addColumn("id", Types.NVARCHAR, String.class)
            .addColumn("name", Types.NVARCHAR, String.class)
            .addColumn("description", Types.NVARCHAR, String.class)
            .addColumn("reference_type", Types.NVARCHAR, String.class)
            .addColumn("reference_id", Types.NVARCHAR, String.class)
            .addColumn("type", Types.NVARCHAR, String.class)
            .addColumn("enabled", Types.BIT, boolean.class)
            .addColumn("severity", Types.NVARCHAR, String.class)
            .addColumn("definition", Types.NVARCHAR, String.class)
            .addColumn("created_at", Types.TIMESTAMP, Date.class)
            .addColumn("updated_at", Types.TIMESTAMP, Date.class)
            .build();

    @Override
    protected JdbcObjectMapper getOrm() {
        return ORM;
    }

    @Override
    protected String getId(final AlertTrigger alert) {
        return alert.getId();
    }

    @Override
    public List<AlertTrigger> findByReference(final String referenceType, final String referenceId) throws TechnicalException {
        LOGGER.debug("JdbcAlertRepository.findByReference({}, {})", referenceType, referenceId);
        try {
            return jdbcTemplate.query("select * from alert_triggers where reference_type = ? and reference_id = ?"
                    , ORM.getRowMapper(), referenceType, referenceId);
        } catch (final Exception ex) {
            final String message = "Failed to find alerts by reference";
            LOGGER.error(message, ex);
            throw new TechnicalException(message, ex);
        }
    }
}