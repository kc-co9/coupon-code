package com.co.kc.couponcode.provider.persistence;

import com.co.kc.couponcode.core.CouponCodePool;
import com.co.kc.couponcode.core.algo.Lcg;
import com.co.kc.couponcode.core.algo.LcgFactor;
import com.co.kc.couponcode.core.model.IFactor;
import com.co.kc.couponcode.core.persistence.ICodeGen;
import com.co.kc.couponcode.provider.repository.dao.CouponCodeGeneratorRepository;
import com.co.kc.couponcode.provider.repository.entities.CouponCodeGenerator;
import com.co.kc.couponcode.provider.repository.enums.CouponCodeGeneratorStatus;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultCodeGenTest {

    private static final IFactor FACTOR = LcgFactor.PERIOD_8589934592;
    private static final long X0 = RandomUtils.nextLong(1, FACTOR.getM());
    private static final long XN = RandomUtils.nextLong(1, FACTOR.getM());

    @Test
    public void testConcurrentlySelectIfRepositoryIsEmpty() throws InterruptedException, ExecutionException {
        CopyOnWriteArrayList<CouponCodeGenerator> memoryRepository = new CopyOnWriteArrayList<>();
        CouponCodeGeneratorRepository mockRepository = getMockRepository(memoryRepository, 1, 1);

        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(1000, 1000, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            tasks.add(() -> {
                new DefaultCodeGen(LcgFactor.PERIOD_8589934592, mockRepository).select();
                return true;
            });
        }
        List<Future<Boolean>> futures = threadPoolExecutor.invokeAll(tasks);
        for (Future<Boolean> future : futures) {
            future.get();
        }
        Assert.assertEquals(1000, memoryRepository.size());
    }

    @Test
    public void testConcurrentlySelectIfExistInactiveGenerator() throws InterruptedException, ExecutionException {
        CopyOnWriteArrayList<CouponCodeGenerator> memoryRepository = new CopyOnWriteArrayList<>();
        CouponCodeGenerator inactiveGenerator = new CouponCodeGenerator();
        inactiveGenerator.setId(1L);
        inactiveGenerator.setNo(1L);
        inactiveGenerator.setX0(X0);
        inactiveGenerator.setXn(X0);
        inactiveGenerator.setCnt(0L);
        inactiveGenerator.setA(FACTOR.getA());
        inactiveGenerator.setC(FACTOR.getC());
        inactiveGenerator.setM(FACTOR.getM());
        inactiveGenerator.setStatus(CouponCodeGeneratorStatus.INACTIVE);
        inactiveGenerator.setHeartbeatAt(LocalDateTime.now());
        inactiveGenerator.setCreatedAt(LocalDateTime.now());
        inactiveGenerator.setUpdatedAt(LocalDateTime.now());
        memoryRepository.add(inactiveGenerator);

        CouponCodeGenerator heartTimeoutGenerator = new CouponCodeGenerator();
        heartTimeoutGenerator.setId(2L);
        heartTimeoutGenerator.setNo(2L);
        heartTimeoutGenerator.setX0(X0);
        heartTimeoutGenerator.setXn(X0);
        heartTimeoutGenerator.setCnt(0L);
        heartTimeoutGenerator.setA(FACTOR.getA());
        heartTimeoutGenerator.setC(FACTOR.getC());
        heartTimeoutGenerator.setM(FACTOR.getM());
        heartTimeoutGenerator.setStatus(CouponCodeGeneratorStatus.ACTIVATED);
        heartTimeoutGenerator.setHeartbeatAt(LocalDateTime.now().minus(10, ChronoUnit.MINUTES));
        heartTimeoutGenerator.setCreatedAt(LocalDateTime.now());
        heartTimeoutGenerator.setUpdatedAt(LocalDateTime.now());
        memoryRepository.add(heartTimeoutGenerator);

        CouponCodeGeneratorRepository mockRepository = getMockRepository(memoryRepository, 3, 3);
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(1000, 1000, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            tasks.add(() -> {
                new DefaultCodeGen(LcgFactor.PERIOD_8589934592, mockRepository).select();
                return true;
            });
        }
        List<Future<Boolean>> futures = threadPoolExecutor.invokeAll(tasks);
        for (Future<Boolean> future : futures) {
            future.get();
        }
        Assert.assertEquals(1001, memoryRepository.size());
    }

    @Test
    public void testConcurrentlySelectIfExistActiveGenerator() throws InterruptedException, ExecutionException {
        CopyOnWriteArrayList<CouponCodeGenerator> memoryRepository = new CopyOnWriteArrayList<>();
        CouponCodeGenerator activeGenerator = new CouponCodeGenerator();
        activeGenerator.setId(1L);
        activeGenerator.setNo(1L);
        activeGenerator.setX0(X0);
        activeGenerator.setXn(X0);
        activeGenerator.setCnt(0L);
        activeGenerator.setA(FACTOR.getA());
        activeGenerator.setC(FACTOR.getC());
        activeGenerator.setM(FACTOR.getM());
        activeGenerator.setStatus(CouponCodeGeneratorStatus.ACTIVATED);
        activeGenerator.setHeartbeatAt(LocalDateTime.now());
        activeGenerator.setCreatedAt(LocalDateTime.now());
        activeGenerator.setUpdatedAt(LocalDateTime.now());
        memoryRepository.add(activeGenerator);

        CouponCodeGeneratorRepository mockRepository = getMockRepository(memoryRepository, 3, 3);
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(1000, 1000, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            tasks.add(() -> {
                new DefaultCodeGen(LcgFactor.PERIOD_8589934592, mockRepository).select();
                return true;
            });
        }
        List<Future<Boolean>> futures = threadPoolExecutor.invokeAll(tasks);
        for (Future<Boolean> future : futures) {
            future.get();
        }
        Assert.assertEquals(1001, memoryRepository.size());
    }

    @Test
    public void testFlushIfNotUsedUp() {
        CopyOnWriteArrayList<CouponCodeGenerator> memoryRepository = new CopyOnWriteArrayList<>();
        CouponCodeGenerator inactiveGenerator = new CouponCodeGenerator();
        inactiveGenerator.setId(1L);
        inactiveGenerator.setNo(1L);
        inactiveGenerator.setX0(X0);
        inactiveGenerator.setXn(X0);
        inactiveGenerator.setCnt(0L);
        inactiveGenerator.setA(FACTOR.getA());
        inactiveGenerator.setC(FACTOR.getC());
        inactiveGenerator.setM(FACTOR.getM());
        inactiveGenerator.setStatus(CouponCodeGeneratorStatus.INACTIVE);
        inactiveGenerator.setHeartbeatAt(LocalDateTime.now());
        inactiveGenerator.setCreatedAt(LocalDateTime.now());
        inactiveGenerator.setUpdatedAt(LocalDateTime.now());
        memoryRepository.add(inactiveGenerator);

        CouponCodeGeneratorRepository mockRepository = getMockRepository(memoryRepository, 2, 2);
        ICodeGen codeGen = new DefaultCodeGen(LcgFactor.PERIOD_8589934592, mockRepository);

        codeGen.select();
        Assert.assertEquals(CouponCodeGeneratorStatus.ACTIVATED, inactiveGenerator.getStatus());

        codeGen.flush(1L, new CouponCodePool.PoolCode(1, XN, FACTOR.getFormat()), 1L);
        Assert.assertEquals(CouponCodeGeneratorStatus.ACTIVATED, inactiveGenerator.getStatus());
    }

    @Test
    public void testFlushIfUsedUp() {
        CopyOnWriteArrayList<CouponCodeGenerator> memoryRepository = new CopyOnWriteArrayList<>();
        CouponCodeGenerator inactiveGenerator = new CouponCodeGenerator();
        inactiveGenerator.setId(1L);
        inactiveGenerator.setNo(1L);
        inactiveGenerator.setX0(Lcg.next(FACTOR, XN));
        inactiveGenerator.setXn(XN);
        inactiveGenerator.setCnt(0L);
        inactiveGenerator.setA(FACTOR.getA());
        inactiveGenerator.setC(FACTOR.getC());
        inactiveGenerator.setM(FACTOR.getM());
        inactiveGenerator.setStatus(CouponCodeGeneratorStatus.INACTIVE);
        inactiveGenerator.setHeartbeatAt(LocalDateTime.now());
        inactiveGenerator.setCreatedAt(LocalDateTime.now());
        inactiveGenerator.setUpdatedAt(LocalDateTime.now());
        memoryRepository.add(inactiveGenerator);

        CouponCodeGeneratorRepository mockRepository = getMockRepository(memoryRepository, 2, 2);
        ICodeGen codeGen = new DefaultCodeGen(LcgFactor.PERIOD_8589934592, mockRepository);

        codeGen.select();
        Assert.assertEquals(CouponCodeGeneratorStatus.ACTIVATED, inactiveGenerator.getStatus());

        codeGen.flush(1L, new CouponCodePool.PoolCode(1, XN, FACTOR.getFormat()), 1L);
        Assert.assertEquals(CouponCodeGeneratorStatus.INVALID, inactiveGenerator.getStatus());
    }

    private CouponCodeGeneratorRepository getMockRepository(CopyOnWriteArrayList<CouponCodeGenerator> memoryRepository, int startId, int startNo) {
        AtomicLong idIndex = new AtomicLong(startId);
        AtomicLong noIndex = new AtomicLong(startNo);

        CouponCodeGeneratorRepository mockRepository = mock(CouponCodeGeneratorRepository.class);

        when(mockRepository.getNextNo()).thenAnswer((Answer<Long>) invocation -> noIndex.getAndIncrement());

        when(mockRepository.getInactiveUpdateList()).thenAnswer((Answer<List<CouponCodeGenerator>>) invocation ->
                memoryRepository.stream()
                        .filter(o -> CouponCodeGeneratorStatus.ACTIVATED.equals(o.getStatus()))
                        .filter(o -> LocalDateTime.now().minus(1, ChronoUnit.MINUTES).compareTo(o.getHeartbeatAt()) >= 0)
                        .collect(Collectors.toList()));
        when(mockRepository.getInactiveList()).thenAnswer((Answer<List<CouponCodeGenerator>>) invocation ->
                memoryRepository.stream()
                        .filter(o -> CouponCodeGeneratorStatus.INACTIVE.equals(o.getStatus()))
                        .collect(Collectors.toList()));

        when(mockRepository.updateStatusByIdIfLossHeartAndMeetExpectStatus(anyLong(), any(), any()))
                .thenAnswer((Answer<Boolean>) invocation -> {
                    Long id = invocation.getArgument(0, Long.class);
                    CouponCodeGeneratorStatus expected = invocation.getArgument(1, CouponCodeGeneratorStatus.class);
                    CouponCodeGeneratorStatus updatedStatus = invocation.getArgument(2, CouponCodeGeneratorStatus.class);
                    boolean isUpdated = false;
                    for (int i = 0; i < memoryRepository.size(); i++) {
                        CouponCodeGenerator couponCodeGenerator = memoryRepository.get(i);
                        if (id.equals(couponCodeGenerator.getId())) {
                            synchronized (memoryRepository) {
                                couponCodeGenerator = memoryRepository.get(i);
                                if (expected.equals(couponCodeGenerator.getStatus())
                                        && LocalDateTime.now().minus(1, ChronoUnit.MINUTES).compareTo(couponCodeGenerator.getHeartbeatAt()) >= 0) {
                                    isUpdated = true;
                                    couponCodeGenerator.setStatus(updatedStatus);
                                    memoryRepository.set(i, couponCodeGenerator);
                                }
                            }
                            break;
                        }
                    }
                    return isUpdated;
                });
        when(mockRepository.updateStatusByIdIfExpectStatus(anyLong(), any(), any()))
                .thenAnswer((Answer<Boolean>) invocation -> {
                    Long id = invocation.getArgument(0, Long.class);
                    CouponCodeGeneratorStatus expected = invocation.getArgument(1, CouponCodeGeneratorStatus.class);
                    CouponCodeGeneratorStatus updatedStatus = invocation.getArgument(2, CouponCodeGeneratorStatus.class);
                    boolean isUpdated = false;
                    for (int i = 0; i < memoryRepository.size(); i++) {
                        CouponCodeGenerator couponCodeGenerator = memoryRepository.get(i);
                        if (id.equals(couponCodeGenerator.getId())) {
                            synchronized (memoryRepository) {
                                couponCodeGenerator = memoryRepository.get(i);
                                if (expected.equals(couponCodeGenerator.getStatus())) {
                                    isUpdated = true;
                                    couponCodeGenerator.setStatus(updatedStatus);
                                    memoryRepository.set(i, couponCodeGenerator);
                                }
                            }
                            break;
                        }
                    }
                    return isUpdated;
                });

        when(mockRepository.updateHeartbeatAtById(anyLong()))
                .thenAnswer((Answer<Boolean>) invocation -> {
                    Long id = invocation.getArgument(0, Long.class);
                    for (int i = 0; i < memoryRepository.size(); i++) {
                        CouponCodeGenerator couponCodeGenerator = memoryRepository.get(i);
                        if (id.equals(couponCodeGenerator.getId())) {
                            couponCodeGenerator.setHeartbeatAt(LocalDateTime.now());
                            memoryRepository.set(i, couponCodeGenerator);
                            break;
                        }
                    }
                    return Boolean.TRUE;
                });

        when(mockRepository.insertGenerator(any()))
                .thenAnswer((Answer<Boolean>) invocation -> {
                    CouponCodeGenerator couponCodeGenerator = invocation.getArgument(0, CouponCodeGenerator.class);
                    couponCodeGenerator.setId(idIndex.getAndIncrement());
                    return memoryRepository.add(couponCodeGenerator);
                });
        when(mockRepository.updateCodeGenByNo(anyLong(), anyLong(), anyLong(), anyInt()))
                .thenAnswer((Answer<Boolean>) invocation -> {
                    Long no = invocation.getArgument(0, Long.class);
                    Long xn = invocation.getArgument(1, Long.class);
                    Long delta = invocation.getArgument(2, Long.class);
                    Integer status = invocation.getArgument(3, Integer.class);
                    for (int i = 0; i < memoryRepository.size(); i++) {
                        CouponCodeGenerator couponCodeGenerator = memoryRepository.get(i);
                        if (no.equals(couponCodeGenerator.getNo())) {
                            couponCodeGenerator.setXn(xn);
                            couponCodeGenerator.setCnt(couponCodeGenerator.getCnt() + delta);
                            //noinspection OptionalGetWithoutIsPresent
                            couponCodeGenerator.setStatus(CouponCodeGeneratorStatus.getEnum(status).get());
                            memoryRepository.set(i, couponCodeGenerator);
                            break;
                        }
                    }
                    return true;
                });

        return mockRepository;
    }
}
