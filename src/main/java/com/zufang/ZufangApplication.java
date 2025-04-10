package com.zufang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.zufang.mapper")
@EnableScheduling
public class ZufangApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(ZufangApplication.class, args);
        System.out.println("  \n" +
                "        /\\_/\\\n" +
                "       ( o.o )\n" +
                " > ^ <  > ^ <   _ooOoo_   > ^ <  > ^ <\n" +
                "              o8888888o\n" +
                "              88\" . \"88\n" +
                "              (| -_- |)\n" +
                "              O\\  =  /O\n" +
                "           __/`---'\\___\n" +
                "          .' \\\\|     |// '.\n" +
                "         / \\\\|||  :  |||// \\\n" +
                "        | _||||| -:- |||||- |\n" +
                "        |   | \\\\  -  /// |   |\n" +
                "        | \\_|  ''\\---/''  |_/ |\n" +
                "        \\  .-\\__ `-` ___/-. /\n" +
                "      ___`. .' /--.--\\ `. . ___\n" +
                "   .\"\" '< `.___\\_<|>_/___.` >' \"\".\n" +
                "  | | : `- \\`.;`\\ _ /`;.`/ - ` : | |\n" +
                "   \\ \\ `-.   \\_ __\\ /__ _/   .-` / /\n" +
                "======`-.____`-.___\\_____/___.-`____.-'======\n" +
                "                   `=---='      \n" +
                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  \n" +
                "         佛祖保佑       永无BUG       \n");
    }

} 