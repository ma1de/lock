# 🌐🔒🔑 Lock 
**A plugin for securing your Minecraft server** <br>

> [!NOTE]
> For less confusion, please take your time and read the README from top to bottom
> <br> If at any point of time you encountered an issue, please, do not contact me through my e-mail, use the Issues tab

# Building
> [!NOTE] 
> For security reasons, this plugin does not have a binary version publicly available on it's GitHub repository

### Step 1: Clone the repository
🗑️ On **Windows** you would need to install [Git SCM](https://git-scm.com/) *(I don't know why you'd want to host a Minecraft server on Windows, so please, use WSL, or just get Linux)* <br>
🐧 On **Linux** you would need to install `git` using your package manager <br> <br>
Here's some examples for multiple distributions: <br>
- **Debian based distribution**: `apt install git`
- **Fedora**: `dnf install git`
- **Arch**: `pacman -S git`
- **Gentoo**: `emerge --ask dev-vcs/git`

Clone the repository by executing this command:

    git clone https://github.com/ma1de/lock.git

### Step 2: Build the plugin
☕ Make sure that you have **JDK 21** installed on your system, if not, than you can
get it from Eclipse Temurin. <br>
CD into the `lock` directory and perform the next command:

    ./gradlew shadowJar

### Step 3: Install the plugin
Now that you have compiled it you can go into the `build/libs` directory, copy
the `Lock-all.jar` *(look for a file with the `-all` ending, it's important)*  file, and paste it into your `plugins/` folder. <br>
Enjoy!

# License Notice

    Lock - A plugin for securing your Minecraft server
    Copyright (C) 2024  ma1de 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    See LICENSE.txt in this repository for the copy of the GNU General
    Public License.