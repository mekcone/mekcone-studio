/*
Copyright © 2023 Tenton Lien

*/
package main

import (
	"encoding/json"
	"encoding/xml"
	"excode-cli/entity"
	"excode-cli/util"
	"fmt"
	lua "github.com/yuin/gopher-lua"
	"io/ioutil"
	"layeh.com/gopher-luar"
	"log"
)

func main() {
	bytes, err := ioutil.ReadFile("../examples/e-school.xml")
	if err != nil {
		fmt.Println("failed to read xml file:", err)
		return
	}
	var project entity.Project
	err = xml.Unmarshal(bytes, &project)
	if err != nil {
		return
	}

	if project.Templates != nil {
		for _, template := range project.Templates {
			bytes, err = ioutil.ReadFile(fmt.Sprintf("../templates/%s/config.json", template.Type))
			if err != nil {
				fmt.Println("failed to read xml file:", err)
				return
			}
			templateStr := util.ParseDynamicParams(string(bytes), project, template)
			var templateConfig entity.TemplateConfig
			err = json.Unmarshal([]byte(templateStr), &templateConfig)
			if err != nil {
				return
			}
			generate(templateConfig, project, template)
		}
	}

	//cmd.Execute()
}

func generate(config entity.TemplateConfig, project entity.Project, template entity.Template) {
	fmt.Println("generating...")
	baseUrl := "../output" // + time.Now().Format("20060102150405")
	err := util.CreateDir(baseUrl)
	if err != nil {
		panic(err)
	}
	traverseStructure(config.Structure, baseUrl, config.Name, project, template)
}

func traverseStructure(structure entity.Structure, baseUrl string, templateName string,
	project entity.Project, template entity.Template) {
	if structure.Type == "dir" {
		baseUrl += "/" + structure.Name
		err := util.CreateDir(baseUrl)
		if err != nil {
			panic(err)
		}
		if structure.Generator != "" {
			ret := ExecLuaScript(templateName, structure.Generator, project, template, structure.Source)
			table := ret.(*lua.LTable)
			table.ForEach(func(L1 lua.LValue, L2 lua.LValue) {
				dstFileName := baseUrl + "/" + L1.String()
				err = ioutil.WriteFile(dstFileName, []byte(L2.String()), 0666)
			})
		}
	} else if structure.Type == "file" {

		dstFileName := baseUrl + "/" + structure.Name
		srcFileName := fmt.Sprintf("../templates/%s/%s", templateName, structure.Source)
		if structure.Dynamic {
			templateFile, err := ioutil.ReadFile(srcFileName)
			if err != nil {
				log.Printf("Failed to read template file: %s", srcFileName)
				return
			}
			templateFile = []byte(util.ParseDynamicParams(string(templateFile), project, template))
			err = ioutil.WriteFile(dstFileName, templateFile, 0666)
			if err != nil {
				log.Printf("Failed to write file: %s", dstFileName)
			}
		} else if structure.Transformer != "" {
			ret := ExecLuaScript(templateName, structure.Transformer, project, template, structure.Source)
			if ret != nil && ret.String() != "nil" {
				table := ret.(*lua.LTable)
				table.ForEach(func(L1 lua.LValue, L2 lua.LValue) {
					if L1 != nil && L2 != nil {
						dstFileName := baseUrl + "/" + L1.String()
						err := ioutil.WriteFile(dstFileName, []byte(L2.String()), 0666)
						if err != nil {
							log.Printf("Failed to write tranformed files(%s): %s", dstFileName, err)
						}
					}
				})
			}
		} else {
			err := util.CopyFile(dstFileName, srcFileName)
			if err != nil {
				panic(err)
			}
		}
	}

	if structure.Children != nil {
		for _, child := range structure.Children {
			traverseStructure(child, baseUrl, templateName, project, template)
		}
	}
}

func ExecLuaScript(templateName string, funcName string, project entity.Project, template entity.Template, source string) lua.LValue {
	L := lua.NewState()
	defer L.Close()

	if err := L.DoFile(fmt.Sprintf("../templates/%s/transform.lua", templateName)); err != nil {
		panic(err)
	}

	L.SetGlobal("project", luar.New(L, project))
	L.SetGlobal("template", luar.New(L, template))

	if source != "" {
		srcFileName := fmt.Sprintf("../templates/%s/%s", templateName, source)
		sourceBytes, err := ioutil.ReadFile(srcFileName)
		if err != nil {
			log.Printf("Failed to read source file before execute Lua script: %s", srcFileName)
			return nil
		}
		sourceStr := util.ParseDynamicParams(string(sourceBytes), project, template)
		L.SetGlobal("source", lua.LString(sourceStr))
	}

	err := L.CallByParam(lua.P{
		Fn:      L.GetGlobal(funcName),
		NRet:    1,
		Protect: true,
	})
	if err != nil {
		log.Printf("Failed to execute lua function %s: %s", funcName, err)
		return nil
	}
	return L.Get(-1)
}
