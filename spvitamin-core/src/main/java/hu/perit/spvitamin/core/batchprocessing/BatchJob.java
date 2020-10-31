/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.core.batchprocessing;

import java.util.concurrent.Callable;

import hu.perit.spvitamin.core.StackTracer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Peter Nagy
 */

@Log4j2
@Getter
@Setter
public abstract class BatchJob implements Callable<Boolean> {

    // Ez a status a batch-hez tartozó összes job-ban ugyanarra az objektumra hivatkozik, így detektálni lehet, hogy fellépett egy
    // fatal error, ilyenkor a batch feldolgozását meg kell szakítani, illetve ha még csak eteti az ExecutorService-t,
    // akkor abba kell hagyni az etetést
    private BatchJobStatus status;

    @Override
    @SuppressWarnings("squid:S1193")
    public Boolean call() throws Exception {
        try {
            return this.execute();
        }
        catch (Exception ex) {
            boolean fatal = this.isFatalException(ex);

            if (!(ex instanceof InterruptedException)) {
                if (ex instanceof NullPointerException) {
                    if (fatal) {
                        log.error(StackTracer.toString(ex));
                    }
                    else {
                        log.warn(StackTracer.toString(ex));
                    }
                }
                else {
                    if (fatal) {
                        log.error(ex.toString());
                    }
                    else {
                        log.warn(ex.toString());
                    }
                }
            }

            if (fatal && this.status != null) {
                this.status.setFatalError(true);
            }
            throw ex;
        }
    }

    protected abstract Boolean execute() throws Exception; // NOSONAR

    public abstract boolean isFatalException(Throwable ex);
}
