# 基于 Spring Boot 的个性化在线学习平台

（MVP · 本地上传优先 · 邮箱/短信 OTP 登录 · Spring AI/DeepSeek · **Redis 驱动验证码/缓存**）

## 目标与范围

- 目标：**能跑通、少 bug、中文注释多、可演示**
- 闭环：登录（OTP+JWT） → 课程/章节/资源（**本地上传**） → 选课+进度 → 作业线（优先） → 推荐/统计
- AI 增强：**AI 推荐重排**、**作业批改与评语**、**学习助手对话**、**题目生成**
- Redis 用途：
  1. **验证码（OTP）存储**（`otp:{channel}:{receiver}`，TTL=10 分钟）
  2. **发送频率限流**（`otp:rate:{receiver}`，TTL=60 秒）
  3. **热门/推荐缓存**（`cache:recommend:*`）
  4. **登录黑名单/登出失效（可选）**
- 非目标：视频转码、直播等商业化复杂能力

------

## 0. 本地环境

- JDK 17、Maven 3.9+、Node.js 18 LTS
- **MySQL 8（root / 123456）**（或 H2）
- **Redis 6+（本机服务，无 Docker）**
- IDE：IntelliJ IDEA（建议装 Cursor 插件）
- 不使用 Docker；文件**直接存本地磁盘**（如 `D:/learning_uploads` 或 `~/learning_uploads`）

------

## 1. 技术栈

- **后端**：Spring Boot 3.x、Spring Web、Spring Security + JWT、MyBatis-Plus、MySQL/H2、Lombok、Validation、springdoc-openapi-ui（Swagger）、**Spring Data Redis + Lettuce**、**Spring Cache（RedisCacheManager）**
- **登录**：邮箱/短信 **OTP** + JWT，三角色（STUDENT/TEACHER/ADMIN）
- **文件**：本地存储 → 通过 `/files/**` 可直接访问
- **AI**：**Spring AI**（OpenAI 兼容） + **DeepSeek**（OpenAI 兼容 API），支持 Mock
- **前端**：Vue 3、Vite、Vue Router、Pinia、Element Plus、Axios

------

## 2. 配置（`application.yml`）

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learning?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8
    username: root
    password: 123456
  servlet:
    multipart:
      max-file-size: 200MB
      max-request-size: 200MB

# Redis（本机默认端口）
spring:
  data:
    redis:
      host: localhost
      port: 6379
      # password: ""   # 如有设置请填写
  cache:
    type: redis

# 本地文件存储（优先本地；必须先创建该文件夹）
storage:
  local-path: D:/learning_uploads
  public-prefix: /files/

# JWT
jwt:
  secret: "change_this_to_a_long_random_secret"
  expireMinutes: 1440

# OTP 登录（邮箱/短信）——实际发送服务可 Mock
otp:
  channel: "email"      # 可选: email / sms
  expireMinutes: 10
  rateLimitSeconds: 60  # 发送间隔限制
  email:
    from: "noreply@example.com"
    provider: "SMTP_OR_SENDGRID"  # 【未完成】真实接入
  sms:
    provider: "TWILIO_OR_ALIYUN"  # 【未完成】真实接入

# === Spring AI / DeepSeek ===
spring:
  ai:
    openai:
      base-url: "https://api.deepseek.com"
      api-key: "${DEEPSEEK_API_KEY:}"      # 空则使用 Mock
      chat:
        options:
          model: "deepseek-chat"
          temperature: 0.2
ai:
  mock-enabled: ${AI_MOCK_ENABLED:true}    # true=Mock；有 Key 时设 false
```

------

## 3. 数据库设计（MySQL；可切 H2）

**建库：**

```sql
CREATE DATABASE learning CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- 使用 root/123456 连接
```

> 与 Redis 集成后，**OTP 不再落库**（改存 Redis）。下面表结构用于核心教学数据，保持与前版一致，同时补充 `submission.ai_comment` 字段。

```sql
-- 用户：支持邮箱/手机号；OTP 登录可不填密码
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(20) UNIQUE,
  username VARCHAR(50) UNIQUE,
  password_hash VARCHAR(200),
  role ENUM('STUDENT','TEACHER','ADMIN') NOT NULL DEFAULT 'STUDENT',
  nickname VARCHAR(50),
  avatar VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 课程/章节/资源
CREATE TABLE course (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  cover VARCHAR(255),
  tags VARCHAR(255),
  teacher_id BIGINT NOT NULL,
  status TINYINT DEFAULT 1,
  view_count INT DEFAULT 0,
  enroll_count INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE section (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  sort INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE resource (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  section_id BIGINT,
  type ENUM('video','doc','link') NOT NULL,
  url VARCHAR(255) NOT NULL,
  size BIGINT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 选课/进度/收藏
CREATE TABLE enroll (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_course(user_id, course_id)
);
CREATE TABLE progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  section_id BIGINT NOT NULL,
  progress INT DEFAULT 0,
  last_learn_at TIMESTAMP NULL,
  UNIQUE KEY uk_user_course_section(user_id, course_id, section_id)
);
CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_course_fav(user_id, course_id)
);

-- 作业线（优先实现；测验线可后补）
CREATE TABLE assignment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  deadline DATETIME NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE submission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content TEXT,
  attachment VARCHAR(255),
  score INT NULL,
  ai_comment TEXT,                           -- AI 评语
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  graded_at TIMESTAMP NULL
);

-- 测验线（可后补）
CREATE TABLE quiz (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  quiz_id BIGINT NOT NULL,
  type ENUM('single','judge','text') NOT NULL,
  stem TEXT NOT NULL,
  options_json TEXT,
  answer VARCHAR(255),
  score INT DEFAULT 5
);
CREATE TABLE quiz_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  quiz_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  total_score INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE quiz_answer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  user_answer TEXT,
  is_correct TINYINT,
  score INT DEFAULT 0
);

-- 留言/公告
CREATE TABLE comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT
);
```

**演示数据**

```sql
INSERT INTO user(email,role,nickname) VALUES ('teacher@example.com','TEACHER','教师1');
INSERT INTO course(title,description,tags,teacher_id,view_count,enroll_count)
VALUES ('Spring 基础','入门课程','Java,后端,Spring',1,20,5);
```

------

## 4. 后端结构与 Redis/A I 关键点

**包结构建议：**

```
com.example.learning
 ├─ config        # Security/JWT/Cors/Swagger/Static/RedisCache
 ├─ common        # Result<T>/Exception/Validation/IdUtils
 ├─ auth          # AuthController/LoginService/JwtUtil
 │   ├─ otp       # OtpService, OtpStore(接口), RedisOtpStore, InMemoryOtpStore(test), OtpSender接口, MockEmail/SmsSender
 ├─ ai            # AiService, MockAiService（基于 Spring AI/DeepSeek）
 ├─ modules
 │   ├─ user
 │   ├─ course    # course/section/resource
 │   ├─ learn     # enroll/progress/favorite
 │   ├─ work      # assignment/submission（作业线）
 │   └─ cms       # notice/comment
 └─ storage       # FileStorageService(本地)
```

**Redis 封装（要点）**

- `OtpStore` 接口：
  - `saveCode(channel, receiver, code, ttl)` → Redis `SET otp:{channel}:{receiver} code EX ttl NX`
  - `getCode(channel, receiver)` → `GET`
  - `consumeCode(channel, receiver, code)` → 校验相等后 `DEL`（或设置短 TTL）
- 发送限流：
  - `INCR otp:rate:{receiver}` 并 `EXPIRE` 60 秒；>1 则拦截
- 缓存：
  - Spring Cache 注解：`@Cacheable(value="recommend", key="'home:'+#userId", unless="#result==null")`
  - 或手写 `RedisTemplate` 操作
- 黑名单（可选）：
  - `jwt:blacklist:{jti}`，在登出时写入 TTL=剩余有效期

**静态资源映射（使本地上传可访问）：**

```java
@Configuration
public class StaticConfig implements WebMvcConfigurer {
  @Value("${storage.local-path}") String root;
  @Value("${storage.public-prefix}") String prefix;
  @Override public void addResourceHandlers(ResourceHandlerRegistry r) {
    r.addResourceHandler(prefix + "**")
     .addResourceLocations(Paths.get(root).toUri().toString());
  }
}
```

**CORS（前端 5173）与放行策略**

- 放行：`/api/auth/**`, `/files/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- 其余 `/api/**` 需 `Authorization: Bearer <token>`
- 角色：`STUDENT/TEACHER/ADMIN`

------

## 5. API 设计（含 Redis/AI 与本地上传）

### 5.1 统一响应体

```json
{ "code": 0, "msg": "ok", "data": {} }
```

### 5.2 认证（邮箱/短信 OTP + JWT，**Redis 存码+限流**）

- `POST /api/auth/otp/send`
   请求：`{"receiver":"teacher@example.com","channel":"email"}` 或 `{"receiver":"13800001111","channel":"sms"}`
   响应：`{"code":0,"msg":"ok"}`
   行为：
  - 限流：`otp:rate:{receiver}` 每 60 秒 1 次
  - 生成 6 位数字，存 `otp:{channel}:{receiver}` TTL=10 分钟
  - **实际发送【未完成】**，默认 `MockOtpSender`（日志打印）
- `POST /api/auth/otp/login`
   请求：`{"receiver":"teacher@example.com","channel":"email","code":"123456"}`
   行为：校验 Redis 中验证码并消费；首次登录自动建用户；返回 JWT
   响应：`{"code":0,"data":{"token":"...","role":"TEACHER"}}`
- `GET /api/auth/profile` → 当前用户信息

### 5.3 课程/章节/资源（**本地上传优先**）

- `GET /api/courses?kw=&tag=&sort=hot&page=1&size=10`

- `GET /api/courses/{id}`（详情含章节与资源）

- （教师）`POST/PUT/DELETE /api/courses`

- （教师）`POST /api/sections` `{courseId,title,sort}`

- （教师）`POST /api/resources/upload`（表单：`file, courseId, sectionId?, type=video|doc|link`）
   响应：

  ```json
  {"code":0,"data":{"url":"/files/2025/09/xxx.mp4","size":123456,"type":"video"}}
  ```

### 5.4 选课/进度/收藏

- `POST /api/enroll/{courseId}`
- `PUT /api/progress` `{courseId, sectionId, progress}`  // 0~100
- `POST /api/fav/{courseId}` / `DELETE /api/fav/{courseId}`

### 5.5 作业线（优先实现）

- （教师）`POST /api/assignments` `{courseId,title,deadline?}`
- `POST /api/submissions` `{assignmentId, content, attachment?}`
- （教师）`PUT /api/submissions/{id}/score` `{score}`

#### 5.5.1 作业 **AI 批改与评语**（Spring AI/DeepSeek）

- `POST /api/ai/grade`
   请求：

  ```json
  {
    "assignmentId": 1,
    "userId": 2,
    "content": "我的答案……",
    "rubric": "评分标准（可选）",
    "maxScore": 100
  }
  ```

  响应（AI/Mock）：

  ```json
  {
    "code":0,
    "data":{"score":85,"comment":"论证清晰，但缺少示例。可补充代码片段说明。"}
  }
  ```

  行为：调用 Spring AI；写回 `submission.score`/`submission.ai_comment`；教师可二次覆盖

### 5.6 推荐/统计/留言/公告（含 **AI 重排**、**Redis 缓存**）

- `GET /api/recommend/home` → `{hot:[], byTags:[], personal:[]}`

  - 基础候选：热门（`ORDER BY enroll_count + view_count DESC`）、同标签（`tags REGEXP`）、个人偏好（收藏/最近学习标签）

  - **缓存**：`@Cacheable("recommend")`（key 含 userId）

  - **AI 重排（可选）**：`POST /api/ai/recommend/rerank`

    ```json
    {"userId":2,"candidates":[{"id":1,"title":"Spring基础","tags":"Java,后端"}]}
    ```

- 统计：

  - `GET /api/stats/my` → `{enrolled, finished, hours}`
  - `GET /api/stats/course/{courseId}` → `{enrollCount, finishRate}`

- 留言/公告：

  - `GET/POST /api/courses/{id}/comments`
  - `GET/POST /api/notices`

### 5.7 学习助手对话 & 题目生成（AI）

- `POST /api/ai/assistant/chat`：`{"courseId":1,"message":"解释依赖注入","history":[...]}` → `{"reply":"..."}`
- `POST /api/ai/quiz/generate`（教师端）：`{"sectionId":10,"types":["single","judge","text"],"count":5}` → 题目数组（可直接写入 `question` 表）

------

## 6. Spring AI（DeepSeek）与 Redis 依赖/示例

**`pom.xml` 关键依赖**

```xml
<!-- Spring AI (OpenAI 兼容) -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
  <version>1.0.0-M2</version>
</dependency>

<!-- Redis -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Spring Cache (已包含在 web/redis 组合中，使用 CacheManager=Redis) -->

<!-- 其余：web, security, mybatis-plus, validation, lombok, springdoc-openapi-ui 等 -->
```

**AI 服务封装（示意）**

```java
@Service
public class AiService {
  private final ChatClient chat;
  private final boolean mock;

  public AiService(ChatClient chat, @Value("${ai.mock-enabled:true}") boolean mock) {
    this.chat = chat; this.mock = mock;
  }

  public GradeResult grade(String answer, String rubric, int maxScore) {
    if (mock) return new GradeResult(85, "Mock：论证清晰，建议补充示例。");
    String sys = "你是严格但公平的阅卷老师，返回JSON：{score:int, comment:string}";
    String user = "评分标准：" + (rubric==null?"":rubric) + "\n学生答案：" + answer + "\n满分：" + maxScore;
    var out = chat.call(new Prompt(List.of(new SystemMessage(sys), new UserMessage(user))))
                  .getResult().getOutput().getContent();
    // 解析 JSON → GradeResult（注意异常兜底）
    ...
  }
}
```

**OTP 存储抽象（示意）**

```java
public interface OtpStore {
  boolean rateLimit(String receiver, int seconds); // true=触发限流
  void save(String channel, String receiver, String code, Duration ttl);
  Optional<String> get(String channel, String receiver);
  boolean consume(String channel, String receiver, String code); // 校验并删除
}
```

------

## 7. 前端（Vue3 + Vite + Element Plus）关键页面（与前版一致并完整重述）

**创建工程**

```bash
npm create vite@latest learning-web -- --template vue
cd learning-web
npm i axios element-plus pinia vue-router
```

**`src/main.ts`**

```ts
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
createApp(App).use(createPinia()).use(router).use(ElementPlus).mount('#app')
```

**`src/router/index.ts`**

```ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../stores/auth'
import Login from '../pages/Login.vue'
import Home from '../pages/Home.vue'
import Courses from '../pages/Courses.vue'
import CourseDetail from '../pages/CourseDetail.vue'
import Me from '../pages/Me.vue'
import Teach from '../pages/Teach.vue'
import AdminNotices from '../pages/AdminNotices.vue'
import Assistant from '../pages/Assistant.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/', component: Home, meta: { auth: true } },
  { path: '/courses', component: Courses, meta: { auth: true } },
  { path: '/course/:id', component: CourseDetail, meta: { auth: true } },
  { path: '/me', component: Me, meta: { auth: true } },
  { path: '/teach', component: Teach, meta: { auth: true } },
  { path: '/admin/notices', component: AdminNotices, meta: { auth: true } },
  { path: '/assistant', component: Assistant, meta: { auth: true } }
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to, _, next) => {
  const auth = useAuth()
  if (to.meta.auth && !auth.token) return next('/login'); next()
})
export default router
```

**`src/stores/auth.ts`**

```ts
import { defineStore } from 'pinia'
export const useAuth = defineStore('auth', {
  state: () => ({ token: localStorage.getItem('token') || '', role: '' }),
  actions: {
    setToken(t: string, r: string) { this.token=t; this.role=r; localStorage.setItem('token', t) },
    logout() { this.token=''; this.role=''; localStorage.removeItem('token') }
  }
})
```

**Axios 实例 `src/api/http.ts`**

```ts
import axios from 'axios'
import { useAuth } from '../stores/auth'
const http = axios.create({ baseURL: '/api' })
http.interceptors.request.use(cfg => {
  const auth = useAuth()
  if (auth.token) cfg.headers.Authorization = `Bearer ${auth.token}`
  return cfg
})
http.interceptors.response.use(res => res, err => {
  if (err.response?.status === 401) location.href = '/login'
  return Promise.reject(err)
})
export default http
```

**登录页 `src/pages/Login.vue`（OTP 两步 + 倒计时）**

```vue
<template>
  <el-card class="w-96 mx-auto mt-24">
    <h2>验证码登录</h2>
    <el-form :model="form">
      <el-radio-group v-model="form.channel">
        <el-radio label="email">邮箱</el-radio>
        <el-radio label="sms">短信</el-radio>
      </el-radio-group>
      <el-input v-model="form.receiver" placeholder="邮箱或手机号" class="mt-2"/>
      <div class="flex gap-2 mt-2">
        <el-input v-model="form.code" placeholder="验证码"/>
        <el-button :disabled="count>0" @click="send">{{count>0?count+'s':'发送'}}</el-button>
      </div>
      <el-button type="primary" class="mt-4 w-full" @click="login">登录</el-button>
    </el-form>
  </el-card>
</template>
<script setup lang="ts">
import { ref } from 'vue'; import http from '../api/http'; import { useAuth } from '../stores/auth'
const auth = useAuth()
const form = ref({ channel: 'email', receiver: '', code: '' })
const count = ref(0)
async function send(){
  await http.post('/auth/otp/send', { channel: form.value.channel, receiver: form.value.receiver })
  count.value = 60; const t=setInterval(()=>{count.value--; if(count.value<=0) clearInterval(t)},1000)
}
async function login(){
  const { data } = await http.post('/auth/otp/login', form.value)
  auth.setToken(data.data.token, data.data.role); location.href = '/'
}
</script>
<style scoped>.w-96{width:24rem}.mx-auto{margin:0 auto}.mt-24{margin-top:6rem}.flex{display:flex}.gap-2{gap:.5rem}.mt-2{margin-top:.5rem}.w-full{width:100%}</style>
```

**首页推荐 `src/pages/Home.vue`**

```vue
<template>
  <div class="p-4">
    <h2>首页推荐</h2>
    <h3>热门</h3>
    <el-row :gutter="12"><el-col :span="6" v-for="c in hot" :key="c.id">
      <el-card class="mb-3" @click="$router.push('/course/'+c.id)">{{c.title}}</el-card></el-col></el-row>
    <h3>同标签</h3>
    <el-row :gutter="12"><el-col :span="6" v-for="c in byTags" :key="c.id">
      <el-card class="mb-3" @click="$router.push('/course/'+c.id)">{{c.title}}</el-card></el-col></el-row>
    <h3>个性化</h3>
    <el-row :gutter="12"><el-col :span="6" v-for="c in personal" :key="c.id">
      <el-card class="mb-3" @click="$router.push('/course/'+c.id)">{{c.title}}</el-card></el-col></el-row>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'; import http from '../api/http'
const hot = ref([]); const byTags = ref([]); const personal = ref([])
onMounted(async ()=>{
  const { data } = await http.get('/recommend/home')
  hot.value = data.data.hot; byTags.value = data.data.byTags; personal.value = data.data.personal
})
</script>
```

**课程详情（本地文件播放） `src/pages/CourseDetail.vue`**

```vue
<template>
  <div class="p-4" v-if="course">
    <h2>{{course.title}}</h2><p>{{course.description}}</p>
    <el-button type="primary" @click="enroll">选课</el-button>
    <h3 class="mt-4">章节与资源</h3>
    <el-collapse>
      <el-collapse-item v-for="s in sections" :key="s.id" :title="s.title">
        <ul>
          <li v-for="r in s.resources" :key="r.id">
            <a v-if="r.type!=='video'" :href="r.url" target="_blank">{{r.url}}</a>
            <video v-else :src="r.url" controls style="max-width:100%"></video>
          </li>
        </ul>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'; import { useRoute } from 'vue-router'; import http from '../api/http'
const route = useRoute(); const id = Number(route.params.id)
const course = ref<any>(null); const sections = ref<any[]>([])
onMounted(async ()=>{ const { data } = await http.get(`/courses/${id}`); course.value = data.data.course; sections.value = data.data.sections })
async function enroll(){ await http.post(`/enroll/${id}`) }
</script>
```

**AI 助手页 `src/pages/Assistant.vue`**

```vue
<template>
  <el-card class="m-4">
    <h2>学习助手</h2>
    <el-input v-model="msg" placeholder="向 AI 提问..." @keyup.enter="ask"/>
    <el-button class="mt-2" @click="ask">发送</el-button>
    <div class="mt-4" v-for="(m,i) in hist" :key="i">
      <p><b>{{m.role==='user'?'我':'AI'}}：</b>{{m.text}}</p>
    </div>
  </el-card>
</template>
<script setup lang="ts">
import { ref } from 'vue'; import http from '../api/http'
const msg = ref(''); const hist = ref<{role:string,text:string}[]>([])
async function ask(){
  if(!msg.value) return; const q = msg.value; msg.value=''
  hist.value.push({role:'user',text:q})
  const { data } = await http.post('/ai/assistant/chat',{courseId:0,message:q,history:[]})
  hist.value.push({role:'ai',text:data.data.reply})
}
</script>
<style scoped>.m-4{margin:1rem}.mt-2{margin-top:.5rem}.mt-4{margin-top:1rem}</style>
```

**教师端上传 `src/pages/Teach.vue`（简化）**

```vue
<template>
  <div class="p-4">
    <h2>教师后台</h2>
    <el-form :model="course">
      <el-input v-model="course.title" placeholder="课程标题"/>
      <el-input v-model="course.tags" placeholder="标签，如 Java,后端"/>
      <el-button type="primary" @click="saveCourse">保存课程</el-button>
    </el-form>
    <h3 class="mt-4">上传资源</h3>
    <el-upload :action="uploadUrl" :data="{courseId:courseId, sectionId:sectionId, type:'video'}" :headers="headers" />
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'; import http from '../api/http'
const course = ref<any>({ title:'', tags:'' }); const courseId = ref<number>(0); const sectionId = ref<number>(0)
async function saveCourse(){ const { data } = await http.post('/courses', course.value); courseId.value=data.data.id }
const headers = { Authorization: 'Bearer '+(localStorage.getItem('token')||'') }
const uploadUrl = '/api/resources/upload'
</script>
```

------

## 8. 本地运行步骤

1. **MySQL**：执行“建库与表结构 SQL”（root/123456）。

2. **Redis**：本机启动 `redis-server`（默认 6379，非 Docker）。

3. **上传目录**：创建 `D:/learning_uploads`（或修改 `application.yml`）。

4. **后端**：`mvn spring-boot:run` 或 IDEA 运行 `Application.main()`

5. **前端**：

   ```bash
   npm create vite@latest learning-web -- --template vue
   cd learning-web
   npm i axios element-plus pinia vue-router
   npm run dev
   ```

6. 打开 `http://localhost:5173`，在登录页用 `teacher@example.com` 发送 OTP 登录（默认 Mock 发送）。

> **H2 快速模式（可选）**：切换数据源到 H2，并把 SQL 放到 `src/main/resources/schema.sql`。

------

## 9. 单元测试清单（按模块完成即写）

> **测试环境不依赖本机 Redis**：启用 `test` profile 注入 `InMemoryOtpStore` 与 `MockAiService`。

- **AuthControllerTest**
  - 发送验证码 → 命中限流逻辑（第 2 次 60s 内被拒）
  - 正确验证码登录获 JWT；错误/过期验证码 400
- **CourseControllerTest**
  - 新增课程/章节；
  - 上传资源返回 `/files/**` 可访问 URL；
  - 课程详情带章节与资源
- **LearnControllerTest**
  - 选课去重；进度 0~100 校验与 `last_learn_at` 更新
- **AssignmentControllerTest**（作业线闭环）
  - 发布作业 → 学生提交 → 教师评分；
  - `POST /api/ai/grade` 返回 `{score, comment}` 并落库（Mock）
- **RecommendControllerTest**
  - `/recommend/home` 三段非空；
  - 缓存命中（第二次请求更快 or 命中 @Cacheable）；
  - `POST /api/ai/recommend/rerank` 返回排序得分
- **AssistantControllerTest**
  - `/api/ai/assistant/chat` Mock 模式返回固定文案
- **QuizControllerTest**（**【未完成】可后补**）
  - `/api/ai/quiz/generate` 生成题目并写入 `question`

------

## 10. Cursor 指令块（直接粘贴生成代码）

### 10.1 生成后端（含 Redis/AI，中文注释多）

```
你是资深 Spring Boot 工程师。创建后端工程（Java 17, Spring Boot 3, MyBatis-Plus, Spring Security + JWT, Lombok, Validation, springdoc-openapi-ui, spring-data-redis, spring-ai-openai-starter）。

要求：
1) 根据 README 的表结构生成 entity/mapper/xml/service/controller，所有类/方法/字段写中文注释。
2) OTP 登录（Redis）：
   - OtpStore 接口 + RedisOtpStore 实现：键 otp:{channel}:{receiver}，TTL=10m；rate: otp:rate:{receiver}（60s）。
   - /api/auth/otp/send：限流校验→生成6位码→Redis存并TTL→调用 OtpSender（默认 Mock 打日志）；错误码与消息清晰。
   - /api/auth/otp/login：校验并消费验证码→签发 JWT；首登自动建用户（默认 STUDENT）。
   - /api/auth/profile 返回当前用户。
3) 安全：
   - 放行 /api/auth/**, /files/**, /swagger-ui/**, /v3/api-docs/**；其余 /api/** 需 Bearer。
   - 角色 STUDENT/TEACHER/ADMIN；示例 @PreAuthorize。
4) 本地文件上传：
   - FileStorageService 保存到 storage.local-path，返回 /files/**；白名单 mp4,pdf,docx,png,jpg；大小限制按 yml。
   - StaticConfig/CorsConfig/JwtConfig/RedisCacheConfig（CacheManager=Redis）。
5) 课程/章节/资源、选课/进度/收藏：完整 CRUD 与接口；浏览自增 view_count。
6) 作业线：
   - assignment/submission：出作业、提交、评分。
   - /api/ai/grade：调用 Spring AI（DeepSeek）或 Mock，返回 {score, comment} 并写回数据库。
7) 推荐与统计：
   - /api/recommend/home：热门/同标签/个人偏好；使用 @Cacheable("recommend") 缓存（key含userId）。
   - /api/ai/recommend/rerank：对候选集调用 AI 得分（或 Mock）。
   - /api/stats/my、/api/stats/course/{id}
8) 学习助手与题目生成：
   - /api/ai/assistant/chat、/api/ai/quiz/generate（AI 或 Mock）。
9) Swagger 打开；提供 BCrypt 生成工具类（main 打印 hash）。
10) 测试：
   - test profile 注入 InMemoryOtpStore 与 MockAiService；按 README “单元测试清单”写 JUnit + SpringBootTest。
```

### 10.2 生成前端（Vue3 + Vite + Element Plus）

```
创建前端工程（Vue3 + Vite + Element Plus + Pinia + Vue Router + Axios），所有关键组件写中文注释。

实现：
- 路由：/login, /, /courses, /course/:id, /me, /teach, /admin/notices, /assistant；未登录跳 /login。
- 认证：OTP 两步（/api/auth/otp/send → /api/auth/otp/login），token 存 localStorage。
- Axios 拦截器：自动附带 token；401 跳转 /login。
- 首页：/api/recommend/home 展示热门/同标签/个性化。
- 课程列表/详情：详情页展示章节与资源；<video> 播放 /files/**；提供“选课”“更新进度”。
- 教师端：新增课程/章节；<el-upload> 调用 /api/resources/upload（headers 带 Authorization）。
- AI 助手页：调用 /api/ai/assistant/chat。
- （可选）教师端按钮：/api/ai/quiz/generate 生成题目；作业详情页按钮：/api/ai/grade 自动评分与评语。
样式用 Element Plus 默认，优先跑通数据链路。
```

------

## 11. 验收清单

-  Redis 连接成功；OTP 发送时写入 `otp:*`，且 60s 内二次发送被限流
-  正确验证码登录返回 JWT；错误/过期验证码 400
-  未登录访问受限接口 401；登录后可访问
-  课程/章节/资源 CRUD；本地上传 `/files/**` 可访问
-  选课成功；进度更新 0~100 生效
-  作业线闭环；AI 评分接口可用（或 Mock）
-  推荐三段正常；二次请求命中缓存；AI rerank 返回得分
-  学习助手对话可回复（或 Mock）
-  统计接口随学习行为变化
-  所有单元测试绿色通过

------

## 12. 【未完成】项（明确标注）

- 【未完成】**邮箱实际发送**（SMTP/SendGrid）：完善 `EmailOtpSender`；读取 `otp.email.*`
- 【未完成】**短信实际发送**（Twilio/阿里云）：完善 `SmsOtpSender`；读取 `otp.sms.*`
- 【未完成】**测验线（Quiz）**：接口/页面/单测
- 【未完成】**AI 生产化策略**：失败重试、超时/降级、限流、token 计费展示

