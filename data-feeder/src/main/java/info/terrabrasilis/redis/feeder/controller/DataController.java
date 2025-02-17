package info.terrabrasilis.redis.feeder.controller;

import static info.terrabrasilis.redis.feeder.util.Constants.DATA;
import static info.terrabrasilis.redis.feeder.util.Constants.CONFIG_URI;
import static info.terrabrasilis.redis.feeder.util.Constants.DATA_PATH;
import static info.terrabrasilis.redis.feeder.util.Constants.CONFIG_PATH;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import info.terrabrasilis.redis.feeder.domain.Clazz;
import info.terrabrasilis.redis.feeder.interfaces.Data;
import info.terrabrasilis.redis.feeder.interfaces.Writable;
import info.terrabrasilis.redis.feeder.service.DataClazzService;
import info.terrabrasilis.redis.feeder.service.DataService;
import info.terrabrasilis.redis.feeder.service.FeatureVOService;
import info.terrabrasilis.redis.feeder.service.FilterVOService;
import info.terrabrasilis.redis.feeder.service.LoiVOService;
import info.terrabrasilis.redis.feeder.service.LoinamesVOService;
import info.terrabrasilis.redis.feeder.service.PeriodService;
import info.terrabrasilis.redis.feeder.vo.DataClassVO;
import info.terrabrasilis.redis.feeder.vo.DataFilterVO;
import info.terrabrasilis.redis.feeder.vo.DataLoiLoinamesVO;
import info.terrabrasilis.redis.feeder.vo.DataLoisVO;
import info.terrabrasilis.redis.feeder.vo.DataPeriodVO;
import info.terrabrasilis.redis.feeder.vo.DataVO;
import info.terrabrasilis.redis.feeder.vo.FeatureFilterVO;
import info.terrabrasilis.redis.feeder.vo.FeatureVO;
import info.terrabrasilis.redis.feeder.vo.LoiVO;
import info.terrabrasilis.redis.feeder.vo.PeriodVO;
import java.util.Collections;

/**
 * @author jether.rodrigues
 */
@Controller
//@EnableScheduling
public class DataController implements Serializable {

        private static final String TIME_ZONE = "America/Sao_Paulo";

	/**
	 * 
	 */
	private static final long serialVersionUID = -7113482896095798000L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataController.class);
	
	@Autowired private final DataService dataService;	
	@Autowired private final PeriodService periodService;
	@Autowired private final FeatureVOService featureVOService;
	@Autowired private final DataClazzService dataClassService;
	@Autowired private final LoiVOService loiVOService;
	@Autowired private final LoinamesVOService loinamesVOService;
	@Autowired private final DataClazzService dataClazzService;
        @Autowired private final FilterVOService filterVOService;
	
	@Autowired @Qualifier("dataPostInRedisCliApi") private final Writable writable;
        // try write to disk
        @Autowired @Qualifier("dataWriteJsonInDisk") private final Writable diskwriter;

        public DataController(DataService dataService, PeriodService periodService, FeatureVOService featureVOService, DataClazzService dataClassService, LoiVOService loiVOService, LoinamesVOService loinamesVOService, DataClazzService dataClazzService, Writable writable, Writable diskwriter, FilterVOService filterVOService) {
            this.dataService = dataService;
            this.periodService = periodService;
            this.featureVOService = featureVOService;
            this.dataClassService = dataClassService;
            this.loiVOService = loiVOService;
            this.loinamesVOService = loinamesVOService;
            this.dataClazzService = dataClazzService;
            this.writable = writable;
            this.diskwriter = diskwriter;
            this.filterVOService = filterVOService;
        }

	/**
	 * This service will post DATA_CLASS in REDIS by API
	 */	
        //@Scheduled(cron = "0 08 20 * * *")
	public void redisDataClassFeeder() {
            LOGGER.info("Starting RedisDataClassFeeder: {}", LocalDateTime.now());

            dataService.findAll().forEach(data -> {
                    List<Clazz> classes = new ArrayList<>();
                    dataClassService.findAllByData(data).forEach(dataClass -> {								
                            classes.add(dataClass.getClazz());				
                    });

                    try {
                            // writable.write(DataClassVO.of(data, classes), CONFIG_URI + "classes", data.getName().replaceAll(" ", "_").toLowerCase());
                            diskwriter.write(DataClassVO.of(data, classes), CONFIG_PATH + "classes", data.getName().replaceAll(" ", "_").toLowerCase());
                    } catch (Exception e) {
                            LOGGER.error("Error: {}", e.getMessage());
                    }
            });

            LOGGER.info("Finishing RedisDataClassFeeder: {}", LocalDateTime.now());
	}
	
	/**
	 * This service will post DATA_PERIODS in REDIS by API
	 */	
        //@Scheduled(cron = "0 10 20 * * *")
	public void redisDataPeriodFeeder() {
            LOGGER.info("Starting RedisDataPeriodFeeder: {}", LocalDateTime.now());

            dataService.findAll().forEach(data -> {	
                    List<Data> periods = periodService.findAllByData(data).stream()
                                                                    .map(p -> {
                                                                            return PeriodVO.of(p.getStartDate(), p.getEndDate(), Collections.emptyList());
                                                                    }).collect(Collectors.toList());
                    try {
                            // writable.write(DataPeriodVO.of(data, periods), CONFIG_URI + "periods", data.getName().replaceAll(" ", "_").toLowerCase());
                            diskwriter.write(DataPeriodVO.of(data, periods), CONFIG_PATH + "periods", data.getName().replaceAll(" ", "_").toLowerCase());
                    } catch (Exception e) {
                            LOGGER.error("Error: {}", e.getMessage());
                    }
            });

            LOGGER.info("Finishing RedisDataPeriodFeeder: {}", LocalDateTime.now());
	}
	
	/**
	 * This service will post CONFIG_URI in REDIS by API
	 */
        //@Scheduled(cron = "0 12 20 * * *")
	public void redisDataLoisFeeder() {
            LOGGER.info("Starting RedisDataLoisFeeder: {}", LocalDateTime.now());

            dataService.findAll().forEach(data -> {						
                    try {
                        DataLoisVO dtlois = DataLoisVO.of(data, loiVOService.findAllByData(data.getId()));
                        // writable.write(dtlois, CONFIG_URI + "lois", data.getName().replaceAll(" ", "_").toLowerCase());
                        diskwriter.write(dtlois, CONFIG_PATH + "lois", data.getName().replaceAll(" ", "_").toLowerCase());
                    } catch (Exception e) {
                            LOGGER.error("Error: {}", e.getMessage());
                    }
            });

            LOGGER.info("Finishing RedisDataLoisFeeder: {}", LocalDateTime.now());
	}
	
	/**
	 * This service will post loinames to CONFIG_URI in REDIS by API
	 */
        //@Scheduled(cron = "0 15 20 * * *")
	public void redisDataLoiLoinamesFeeder() {
            LOGGER.info("Starting RedisDataLoiLoinamesFeeder: {}", LocalDateTime.now());

            dataService.findAll().forEach(data -> {
                    List<LoiVO> lois = new ArrayList<>();

                    loiVOService.findAllByData(data.getId()).forEach(loi -> {					
                            lois.add(LoiVO.of(loi.getGid(), loi.getName(), loinamesVOService.findAllByLoi(loi.getGid(), data.getId())));
                    });		

                    try {
                            // writable.write(DataLoiLoinamesVO.of(data, lois), CONFIG_URI + "loinames", data.getName().replaceAll(" ", "_").toLowerCase());
                            diskwriter.write(DataLoiLoinamesVO.of(data, lois), CONFIG_PATH + "loinames", data.getName().replaceAll(" ", "_").toLowerCase());
                    } catch (Exception e) {
                            LOGGER.error("Error: {}", e.getMessage());
                    }
            });

            LOGGER.info("Finishing RedisDataLoiLoinamesFeeder: {}", LocalDateTime.now());
	}
        
        /**
	 * This service will post CONFIG_URI in REDIS by API
	 */
        //@Scheduled(cron = "0 18 20 * * *")
        public void redisDataFilter() {
            LOGGER.info("Starting RedisDataFilter: {}", LocalDateTime.now());
            
            dataService.findAll().forEach(data -> {
                try {
                    // writable.write(DataFilterVO.of(data, filterVOService.findAllByData(data.getId())), CONFIG_URI + "filters", data.getName().replaceAll(" ", "_").toLowerCase());
                    diskwriter.write(data, CONFIG_PATH + "filters", data.getName().replaceAll(" ", "_").toLowerCase());
                } catch (Exception e) {
                    LOGGER.error("Error: {}", e.getMessage());
                }
            });
            
            LOGGER.info("Finishing RedisDataFilter: {}", LocalDateTime.now());
        }

	/**
	 * This service will post DATA by CLASS and PERIOD in REDIS by API
         * @Scheduled(cron = "0 15 23 * * *")
	 */
        //@Scheduled(cron = "0 20 20 * * *")
	public void redisDataFeeder() {
            LOGGER.info("Starting RedisDataFeeder: {}", LocalDateTime.now());

            List<DataVO> datas = new ArrayList<>();

            dataService.findAll().forEach(data -> {			
                    List<Data> periods = new ArrayList<>();

                    dataClazzService.findAllByData(data).forEach(dc -> {
                            periodService.findAllByData(data).forEach(period -> {
                                    List<FeatureFilterVO> list = featureVOService.findAllByDataAndClazzAndPeriod(data.getId(), dc.getIdClazz(), period.getId());

                                    List<Data> features = list.stream().map(f -> {
                                            return FeatureVO.of(f.getLoi(), f.getLoiname(), f.getAreas());
                                    }).collect(Collectors.toList());	

                                    periods.add(PeriodVO.of(period.getStartDate(), period.getEndDate(), features));
                            });
                            datas.add(DataVO.of(data.getName(), dc.getClazzName(), periods));
                    });
            });

            datas.forEach(d -> {
                    try {
                            // writable.write(d, DATA + d.getClazz().toLowerCase(), d.getName().replaceAll(" ", "_").toLowerCase());
                            diskwriter.write(d, DATA_PATH, d.getName().replaceAll(" ", "_").toLowerCase());
                    } catch (Exception e) {
                            LOGGER.error("Error: {}", e.getMessage());
                    }			
            });

            LOGGER.info("Finishing RedisDataFeeder: {}", LocalDateTime.now());
	}

}
